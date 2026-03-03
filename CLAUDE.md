# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

This library provides a familiar JPA-like API for querying data, modeled after `EntityManager`, while making fetch control a first-class concern. Callers specify which associations to eagerly load at the call site using QueryDSL `Path<?>` values (e.g. `QPerson.person.organization()`), which the library translates into a JPA `EntityGraph` at runtime and applies as a fetch hint — type-safe, refactor-friendly, and without query proliferation.

## Package

`io.github.alterioncorp.jpa.fetch`

## Build & Test Commands

```bash
# Build the project
mvn clean install

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=EntityFinderImplJpaTest

# Run a single test method
mvn test -Dtest=EntityFinderImplJpaTest#testFind

# Package without running tests
mvn package -DskipTests
```

## Architecture Overview

This is a small Java library (package `io.github.alterioncorp.jpa.fetch`, groupId `io.github.alterioncorp`) that provides a CDI-injectable wrapper around JPA/Hibernate with fetch control as a first-class concern.

### Interface and implementation

**`EntityFinder` / `EntityFinderImpl`** — Entry point, modeled after `EntityManager`

All methods accept `Path<?>... fetchPaths` (QueryDSL Q-type paths) to build entity graphs on-the-fly via `PathParser` (package-private). Entity graphs are applied as a `jakarta.persistence.fetchgraph` hint (standard JPA).

API surface:
- `find(type, id, fetchPaths...)` — looks up by primary key; clears persistence context before calling `entityManager.find()`
- `find(type, id, properties, fetchPaths...)` — same, with additional JPA hints merged in
- `find(type, id, lockMode, fetchPaths...)` — same, with lock mode
- `find(type, id, lockMode, properties, fetchPaths...)` — same, with both
- `createNamedQuery(queryName, resultClass)` — wraps `entityManager.createNamedQuery()`
- `createQuery(jpql, resultClass)` — wraps `entityManager.createQuery(String, Class)`
- `createQuery(reference)` — wraps `entityManager.createQuery(TypedQueryReference)`; uses `reference.getName()` and `reference.getResultType()` (Jakarta Persistence 3.2)

All three `create*` methods return a `TypedFetchQuery<X>`.

**`TypedFetchQuery<X>`** — Subinterface of `TypedQuery<X>`

Adds `setFetchPaths(Path<?>... fetchPaths)`, which builds an `EntityGraph` from the given paths and applies it as a fetch hint. All `TypedQuery` setters are overridden with covariant `TypedFetchQuery<X>` return types to preserve fluent chaining. Implemented by package-private `TypedFetchQueryImpl<X>`.

`EntityFinderImpl` is an `@ApplicationScoped` CDI bean with `@PersistenceContext(unitName = "default")`, and also exposes a public constructor accepting `EntityManager` for direct use in tests.

**`PathParser`** — static utility that converts Q-type paths into a `PathTree` via `buildTree(Path<?>...)` / `buildTree(String...)`, and individual path strings into a `PathNode` chain via `buildNode(String)`. Also exposes `pathToString(Path<?>)` to normalise a QueryDSL path to a dot-separated attribute string relative to the entity root.

**`PathTree`** — holds a set of root `PathNode`s; merges nodes with the same attribute name on `addNode`. Applied to an `EntityGraph` via `addToGraph`.

**`PathNode`** — one hop in a path; leaf nodes become attribute nodes, nodes with children become subgraphs. Equality and hashing are by attribute name only, enabling prefix-merge.

### Test setup
Tests use Apache Derby (in-memory) via `JpaTestBase`, which creates/destroys the DB per test class. The `unit-test` persistence unit is defined in `src/test/resources/META-INF/persistence.xml`. QueryDSL Q-types for test entities are generated at build time into `target/generated-test-sources/java` by the `maven-compiler-plugin` (execution id `default-testCompile`) using `querydsl-apt:jakarta` as an annotation processor path, with the `querydsl.packageSuffix=.path` option — so Q-types land in `*.path` sub-packages (e.g., `io.github.alterioncorp.jpa.fetch.entities.path.QPerson`). The `build-helper-maven-plugin` registers that directory as a test source root so Eclipse/m2e includes it on the classpath.

When test entity classes change, run `mvn clean test` (not just `mvn test`) to force Q-type regeneration.

Test classes: `EntityFinderImplJpaTest` (JPA integration), `EntityFinderImplMockTest`, `TypedFetchQueryImplTest`, `PathParserTest`, `PathTreeTest`, `PathNodeTest`.

### Key dependencies
- `querydsl-jpa` / `querydsl-core` (`io.github.openfeign.querydsl` fork, v7.x) — compile-scope
- Hibernate 7.x and Jakarta Persistence 3.2 — provided scope
- Jakarta CDI 4 / Inject 2 / Annotation 2 — provided scope
