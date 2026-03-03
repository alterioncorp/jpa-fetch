# jpa-fetch

[![codecov](https://codecov.io/gh/alterioncorp/jpa-fetch/graph/badge.svg)](https://codecov.io/gh/alterioncorp/jpa-fetch)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## Quick start

```java
// Look up by primary key, eagerly fetching organization and its country
Person person = entityFinder.find(Person.class, id,
        QPerson.person.organization().country());
```

```java
// Query with inline JPQL, eagerly fetching organization and role
List<Person> persons = entityFinder
        .createQuery("select p from Person p where p.name = ?1", Person.class)
        .setParameter(1, "Smith")
        .setFetchPaths(QPerson.person.organization(), QPerson.person.role())
        .getResultList();
```

```java
// Named query — same fluent API
List<Person> persons = entityFinder
        .createNamedQuery(Person.QUERY_BY_NAME, Person.class)
        .setParameter(1, "Smith")
        .setFetchPaths(QPerson.person.organization(), QPerson.person.role())
        .getResultList();
```

## Purpose

JPA `EntityGraph` is the right tool for controlling fetch depth at the call site, but its API is verbose, string-based, and non-composable. This library replaces it with [QueryDSL](https://openfeign.github.io/querydsl/)-generated path expressions — composable, refactor-safe, and checked at compile time — while exposing a familiar `EntityManager`-like API.

### The EntityGraph API is verbose and non-composable

Building an `EntityGraph` directly requires string attribute names and manual subgraph chaining:

```java
// Raw EntityGraph API
EntityGraph<Person> graph = em.createEntityGraph(Person.class);
Subgraph<Organization> orgGraph = graph.addSubgraph("organization");
orgGraph.addAttributeNodes("country");
orgGraph.addAttributeNodes("role");

em.find(Person.class, id, Map.of("jakarta.persistence.fetchgraph", graph));
```

String attribute names break silently on rename. Every additional hop requires another `addSubgraph` call. Combining independent paths (e.g. `organization.country` and `role`) means building the tree manually. None of this is checked at compile time.

**JPA Metamodel helps with type safety, but not composability**

The JPA static metamodel (`Person_`, `Organization_`) eliminates string literals but the fundamental structure remains the same — each attribute is an isolated constant with no way to express traversal as a single expression:

```java
// JPA Metamodel: type-safe attribute names, but still manual tree-building
EntityGraph<Person> graph = em.createEntityGraph(Person.class);
graph.addSubgraph(Person_.organization)
     .addAttributeNodes(Organization_.country);

em.find(Person.class, id, Map.of("jakarta.persistence.fetchgraph", graph));
```

### With this library

QueryDSL Q-types generate typed accessor methods whose return values expose further accessors, producing a composable expression that reads like the path it describes:

```java
QPerson.person.organization().country()   // organization → country
QPerson.person.role()                     // role (independent branch)
```

Pass these expressions to `find` and the library builds the `EntityGraph` automatically — merging shared prefixes into a single subgraph, no manual tree-building required:

```java
// No fetch
entityFinder.find(Person.class, id);

// With organization
entityFinder.find(Person.class, id,
    QPerson.person.organization());

// With organization (and country) and role
entityFinder.find(Person.class, id,
    QPerson.person.organization().country(),
    QPerson.person.role());
```

## Integration

### Maven dependency

```xml
<dependency>
    <groupId>io.github.alterioncorp</groupId>
    <artifactId>jpa-fetch</artifactId>
    <version>1.0.0</version>
</dependency>
```

| Library version | Jakarta Persistence |
|-----------------|---------------------|
| 1.0.x           | 3.2                 |

QueryDSL Q-types must be generated for your entities. Configure `maven-compiler-plugin` to use `querydsl-apt` (jakarta classifier) as an annotation processor:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.openfeign.querydsl</groupId>
                <artifactId>querydsl-apt</artifactId>
                <version>7.1</version>
                <classifier>jakarta</classifier>
            </path>
        </annotationProcessorPaths>
        <compilerArgs>
            <arg>-Aquerydsl.packageSuffix=.path</arg>
            <arg>-Aquerydsl.entityAccessors=true</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

With `querydsl.packageSuffix=.path`, Q-types are generated in a `.path` sub-package of the entity's package. For example, an entity `io.github.alterioncorp.example.entities.Person` gets a Q-type at `io.github.alterioncorp.example.entities.path.QPerson`.

### CDI injection

`EntityFinderImpl` is an `@ApplicationScoped` CDI bean bound to `@PersistenceContext(unitName = "default")`. Inject it directly:

```java
@Inject
EntityFinder entityFinder;
```

## Usage

### Querying

`createQuery` and `createNamedQuery` mirror the `EntityManager` methods and return a `TypedFetchQuery<X>` — a `TypedQuery<X>` subinterface with an added `setFetchPaths` method. The full `TypedQuery` API is available, including pagination, parameter binding, and result retrieval.

```java
// Inline JPQL
entityFinder.createQuery("select p from Person p where p.name = ?1", Person.class)
        .setParameter(1, "Smith")
        .setMaxResults(10)
        .setFetchPaths(QPerson.person.organization())
        .getResultList();

// Named query
entityFinder.createNamedQuery(Person.QUERY_BY_NAME, Person.class)
        .setParameter(1, "Smith")
        .setFetchPaths(QPerson.person.organization())
        .getSingleResult();
```

`setFetchPaths` can be called at any position in the chain.

### Finding by primary key

`find` mirrors the `EntityManager.find` overloads:

```java
// By ID
Person person = entityFinder.find(Person.class, id);

// With fetch paths
Person person = entityFinder.find(Person.class, id,
        QPerson.person.organization().country());

// With lock mode
Person person = entityFinder.find(Person.class, id, LockModeType.PESSIMISTIC_WRITE,
        QPerson.person.organization());
```

The persistence context is cleared before each `find` call to avoid returning a cached instance that lacks the requested associations.

### Collection associations

Paths can traverse collection associations using `.any()`. Given:

```java
@Entity
public class Organization {
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "organization")
    private Collection<Person> persons;
}

@Entity
public class Person {
    @ManyToOne(fetch = FetchType.LAZY)
    private Role role;
}
```

Fetch `organization.persons` and each person's `role` in one call:

```java
Organization org = entityFinder.find(Organization.class, id,
        QOrganization.organization.persons.any().role());
```

Both the `persons` collection and the `role` on each person are eagerly loaded. The path `persons.any().role()` implies `persons` is fetched — specifying `persons` separately would be redundant.
