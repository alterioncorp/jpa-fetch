# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

This library provides a familiar JPA-like API for querying data, modeled after `EntityManager`, while making fetch control a first-class concern. Callers specify which associations to eagerly load at the call site via `FetchPath` — either using JPA metamodel attributes (`FetchPaths.fromAttributes(Person_.organization, Organization_.country)`) or QueryDSL Q-type paths (`QPerson.person.organization().country()`). The library translates these into a JPA `EntityGraph` at runtime and applies it as a fetch hint — type-safe, refactor-friendly, and without query proliferation.

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

This is a small Java library (package `io.github.alterioncorp.jpa.fetch`, groupId `io.github.alterioncorp`) that provides a framework-agnostic wrapper around JPA/Hibernate with fetch control as a first-class concern.

### Interface and implementation

**`FetchPath`** — functional interface (`String[] segments()`) representing a single path through the entity graph as an ordered array of attribute names. All `EntityFinder` and `TypedFetchQuery` methods accept `FetchPath... fetchPaths` as the primary API.

**`FetchPaths`** — factory for creating `FetchPath` instances:
- `FetchPaths.fromAttributes(Attribute<?,?>... attributes)` — builds a `FetchPath` from JPA static metamodel attributes; validates that each attribute's declaring type is compatible with the preceding attribute's target type (uses `PluralAttribute.getElementType()` for collections). Throws `IllegalArgumentException` on an invalid chain.
- `FetchPaths.fromQueryDsl(Path<?>)` — converts a QueryDSL path expression to a `FetchPath` by stripping the root alias and normalising `.any()` calls.

**`EntityFinder` / `EntityFinderImpl`** — Entry point, modeled after `EntityManager`.

`EntityFinder` abstract methods accept `FetchPath... fetchPaths`. QueryDSL `Path<?>...` overloads are default methods that convert via `FetchPaths.fromQueryDsl` and delegate. Explicit no-vararg default methods (`find(type, id)` etc.) resolve Java's varargs ambiguity when called with no fetch paths. Entity graphs are applied as a `jakarta.persistence.fetchgraph` hint (standard JPA).

API surface:
- `find(type, id, fetchPaths...)` — looks up by primary key via `entityManager.find()`
- `find(type, id, properties, fetchPaths...)` — same, with additional JPA hints merged in
- `find(type, id, lockMode, fetchPaths...)` — same, with lock mode
- `find(type, id, lockMode, properties, fetchPaths...)` — same, with both
- `createNamedQuery(queryName, resultClass)` — wraps `entityManager.createNamedQuery()`
- `createQuery(jpql, resultClass)` — wraps `entityManager.createQuery(String, Class)`
- `createQuery(reference)` — wraps `entityManager.createQuery(TypedQueryReference)`; uses `reference.getName()` and `reference.getResultType()` (Jakarta Persistence 3.2)
- `clear()` — delegates to `entityManager.clear()`, detaching all managed entities

All three `create*` methods return a `TypedFetchQuery<X>`.

**`TypedFetchQuery<X>`** — Subinterface of `TypedQuery<X>`

Adds `setFetchPaths(FetchPath... fetchPaths)` (abstract) and `setFetchPaths(Path<?>... fetchPaths)` (default, converts via `FetchPaths.fromQueryDsl`). All `TypedQuery` setters are overridden with covariant `TypedFetchQuery<X>` return types to preserve fluent chaining. Implemented by package-private `TypedFetchQueryImpl<X>`.

`TypedFetchQueryImpl.unwrap(Class<T>)` returns `this` when the requested type is assignable from the wrapper (e.g. `unwrap(TypedFetchQuery.class)`); otherwise delegates to the underlying provider query.

`EntityFinderImpl` exposes a public constructor accepting `EntityManager`, making it usable in any framework (Quarkus, Spring, plain JPA, etc.).

**`PathParser`** — static utility (package-private) that works only with `String[]` segments; no QueryDSL dependency. Converts `FetchPath` instances into a `PathTree` via `buildTree(FetchPath...)`, and individual segment arrays into a `PathNode` chain via `buildNode(String[])`. Throws `IllegalArgumentException` if a segment array is empty.

**`PathTree`** — holds a set of root `PathNode`s; merges nodes with the same attribute name on `addNode`. Applied to an `EntityGraph` via `addToGraph`.

**`PathNode`** — one hop in a path; leaf nodes become attribute nodes, nodes with children become subgraphs. Equality and hashing are by attribute name only, enabling prefix-merge.

### Test setup

Tests use Apache Derby (in-memory) via `JpaTestBase`, which creates/destroys the DB per test class. The `unit-test` persistence unit is defined in `src/test/resources/META-INF/persistence.xml`.

Both `querydsl-apt:jakarta` and `hibernate-processor` run as annotation processors during `default-testCompile`, generating files into `target/generated-test-sources/java`:
- QueryDSL Q-types land in `*.path` sub-packages (e.g. `io.github.alterioncorp.jpa.fetch.entities.path.QPerson`) due to `-Aquerydsl.packageSuffix=.path`
- JPA metamodel classes land in the same package as the entity (e.g. `io.github.alterioncorp.jpa.fetch.entities.Person_`)

The `build-helper-maven-plugin` registers that directory as a test source root so Eclipse/m2e includes it on the classpath.

When test entity classes change, run `mvn clean test` (not just `mvn test`) to force regeneration of Q-types and metamodel classes.

Test classes: `EntityFinderImplJpaTest` (JPA integration), `EntityFinderImplMockTest`, `TypedFetchQueryImplTest`, `FetchPathsTest` (extends `JpaTestBase` to test with real metamodel), `PathParserTest`, `PathTreeTest`, `PathNodeTest`.

### Key dependencies
- `querydsl-jpa` / `querydsl-core` (`io.github.openfeign.querydsl` fork, v7.x) — compile-scope
- Hibernate 7.x and Jakarta Persistence 3.2 — provided scope
