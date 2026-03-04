# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.1] - Unreleased

### Fixed
- `TypedFetchQueryImpl.unwrap(Class<T>)` now correctly returns `this` when the requested type is
  assignable from the wrapper itself (e.g. `unwrap(TypedFetchQuery.class)`), instead of always
  delegating to the underlying provider query and risking a `PersistenceException`.

## [1.0.0] - 2026-03-03

### Added
- Initial release.
- `EntityFinder` / `EntityFinderImpl` — CDI-injectable entry point modelled after `EntityManager`,
  with `find`, `clear`, and `create*Query` methods that accept QueryDSL `Path<?>` values for fetch control.
- `TypedFetchQuery` / `TypedFetchQueryImpl` — `TypedQuery` extension with `setFetchPaths` for
  applying an entity graph fetch hint inline.
- `PathParser`, `PathTree`, `PathNode` — internal utilities for converting QueryDSL paths into
  JPA `EntityGraph` attribute nodes and subgraphs.
