# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2026-03-04

### Added
- `FetchPath` — a functional interface (`String[] segments()`) representing a single path through
  the entity graph as an ordered array of attribute names.
- `FetchPaths` — factory for creating `FetchPath` instances:
  - `FetchPaths.fromAttributeChain(Attribute<?,?>... attributes)` — builds a `FetchPath` from JPA static
    metamodel attributes; validates the attribute chain at call time and throws
    `IllegalArgumentException` if consecutive attributes do not form a valid traversal.
  - `FetchPaths.fromQueryDsl(Path<?>)` — converts a QueryDSL path expression to a `FetchPath`.
- `EntityFinder` and `TypedFetchQuery` now accept `FetchPath... fetchPaths` alongside the existing
  QueryDSL `Path<?>... fetchPaths` overloads. The QueryDSL overloads are now default methods that
  convert via `FetchPaths.fromQueryDsl` and delegate, reducing implementor burden.

### Changed
- `PathParser`, `PathTree`, and `PathNode` no longer have any QueryDSL dependency; they operate
  solely on `String[]` segments. All QueryDSL-to-string conversion now lives in `FetchPaths`.

## [1.0.2] - 2026-03-03

### Changed
- `EntityFinder.find` methods no longer clear the persistence context before executing.
  Callers that need a fresh load should call `clear()` explicitly beforehand.

## [1.0.1] - 2026-03-03

### Changed
- `EntityFinderImpl` no longer carries CDI annotations (`@ApplicationScoped`, `@PersistenceContext`).
  It is now a plain class constructed with an `EntityManager`, making it usable in any framework
  (CDI, Spring, plain JPA). See the README for integration examples.

### Fixed
- `TypedFetchQueryImpl.unwrap(Class<T>)` now correctly returns `this` when the requested type is
  assignable from the wrapper itself (e.g. `unwrap(TypedFetchQuery.class)`), instead of always
  delegating to the underlying provider query and risking a `PersistenceException`.

## [1.0.0] - 2026-03-03

### Added
- Initial release.
- `EntityFinder` / `EntityFinderImpl` — entry point modelled after `EntityManager`,
  with `find`, `clear`, and `create*Query` methods that accept QueryDSL `Path<?>` values for fetch control.
- `TypedFetchQuery` / `TypedFetchQueryImpl` — `TypedQuery` extension with `setFetchPaths` for
  applying an entity graph fetch hint inline.
- `PathParser`, `PathTree`, `PathNode` — internal utilities for converting QueryDSL paths into
  JPA `EntityGraph` attribute nodes and subgraphs.
