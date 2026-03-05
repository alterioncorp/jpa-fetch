# jpa-fetch

[![codecov](https://codecov.io/gh/alterioncorp/jpa-fetch/graph/badge.svg)](https://codecov.io/gh/alterioncorp/jpa-fetch)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A thin wrapper around JPA's `EntityManager` API with first-class fetch control — callers specify which associations to load eagerly using type-safe path expressions, translated into JPA entity graphs at runtime.

Two styles are supported:

- **JPA metamodel** (`FetchPaths.fromAttributeChain`) — uses the standard JPA static metamodel (`Person_`, `Organization_`); validated at call time, no extra dependencies
- **QueryDSL** (Q-type paths) — composable path expressions (`QPerson.person.organization().country()`); requires the QueryDSL APT processor

## Quick start

**JPA metamodel style:**

```java
// Look up by primary key, eagerly fetching organization and its country
Person person = entityFinder.find(Person.class, id,
        FetchPaths.fromAttributeChain(Person_.organization, Organization_.country));
```

```java
// Query with inline JPQL, eagerly fetching organization and role
List<Person> persons = entityFinder
        .createQuery("select p from Person p where p.name = ?1", Person.class)
        .setParameter(1, "Smith")
        .setFetchPaths(
                FetchPaths.fromAttributeChain(Person_.organization),
                FetchPaths.fromAttributeChain(Person_.role))
        .getResultList();
```

**QueryDSL style:**

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

## Purpose

JPA `EntityGraph` is the right tool for controlling fetch depth at the call site, but its API is verbose, string-based, and non-composable. This library replaces it with type-safe path expressions that are checked at compile time and validated at call time, while exposing a familiar `EntityManager`-like API.

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

Pass a `FetchPath` to `find` and the library builds the `EntityGraph` automatically — merging shared prefixes into a single subgraph, no manual tree-building required.

**`FetchPaths.fromAttributeChain`** accepts a chain of JPA metamodel attributes describing a path through the entity graph. The chain is validated at call time — passing attributes that don't form a valid traversal throws `IllegalArgumentException`:

```java
// No fetch
entityFinder.find(Person.class, id);

// With organization
entityFinder.find(Person.class, id,
    FetchPaths.fromAttributeChain(Person_.organization));

// With organization → country and role (two independent paths)
entityFinder.find(Person.class, id,
    FetchPaths.fromAttributeChain(Person_.organization, Organization_.country),
    FetchPaths.fromAttributeChain(Person_.role));
```

**QueryDSL Q-types** generate typed accessor methods whose return values expose further accessors, producing a composable expression that reads like the path it describes:

```java
QPerson.person.organization().country()   // organization → country
QPerson.person.role()                     // role (independent branch)
```

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

Both styles can be mixed freely in the same call.

## Integration

### Maven dependency

```xml
<dependency>
    <groupId>io.github.alterioncorp</groupId>
    <artifactId>jpa-fetch</artifactId>
    <version>1.1.0</version>
</dependency>
```

| Library version | Jakarta Persistence |
|-----------------|---------------------|
| 1.1.x           | 3.2                 |

### JPA Metamodel (for `FetchPaths.fromAttributeChain`)

JPA metamodel classes (`Person_`, `Organization_`, etc.) must be generated for your entities. Configure `maven-compiler-plugin` to use `hibernate-processor` as an annotation processor:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.hibernate.orm</groupId>
                <artifactId>hibernate-processor</artifactId>
                <version>7.0.0.Final</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### QueryDSL Q-types (for QueryDSL path style)

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

### Framework integration

`EntityFinderImpl` accepts an `EntityManager` via its constructor, so it integrates with any framework.

#### CDI (Quarkus, Jakarta EE)

```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class EntityFinderProducer {

    @PersistenceContext
    EntityManager entityManager;

    @Produces
    @ApplicationScoped
    public EntityFinder entityFinder() {
        return new EntityFinderImpl(entityManager);
    }
}
```

Then inject normally:

```java
@Inject
EntityFinder entityFinder;
```

#### Spring

```java
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityFinderConfig {

    @PersistenceContext
    EntityManager entityManager;

    @Bean
    public EntityFinder entityFinder() {
        return new EntityFinderImpl(entityManager);
    }
}
```

Then inject normally:

```java
@Autowired
EntityFinder entityFinder;
```

## Usage

### Querying

`createQuery` and `createNamedQuery` mirror the `EntityManager` methods and return a `TypedFetchQuery<X>` — a `TypedQuery<X>` subinterface with an added `setFetchPaths` method. The full `TypedQuery` API is available, including pagination, parameter binding, and result retrieval.

```java
// Inline JPQL — JPA metamodel style
entityFinder.createQuery("select p from Person p where p.name = ?1", Person.class)
        .setParameter(1, "Smith")
        .setMaxResults(10)
        .setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization))
        .getResultList();

// Inline JPQL — QueryDSL style
entityFinder.createQuery("select p from Person p where p.name = ?1", Person.class)
        .setParameter(1, "Smith")
        .setMaxResults(10)
        .setFetchPaths(QPerson.person.organization())
        .getResultList();

// Named query
entityFinder.createNamedQuery(Person.QUERY_BY_NAME, Person.class)
        .setParameter(1, "Smith")
        .setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization))
        .getSingleResult();

// Named query via JPA Metamodel TypedQueryReference (Jakarta Persistence 3.2)
entityFinder.createQuery(Person_.findByName)
        .setParameter(1, "Smith")
        .setFetchPaths(FetchPaths.fromAttributeChain(Person_.organization))
        .getSingleResult();
```

`setFetchPaths` can be called at any position in the chain.

### Finding by primary key

`find` mirrors the `EntityManager.find` overloads:

```java
// By ID
Person person = entityFinder.find(Person.class, id);

// With fetch paths — JPA metamodel style
Person person = entityFinder.find(Person.class, id,
        FetchPaths.fromAttributeChain(Person_.organization, Organization_.country));

// With fetch paths — QueryDSL style
Person person = entityFinder.find(Person.class, id,
        QPerson.person.organization().country());

// With lock mode
Person person = entityFinder.find(Person.class, id, LockModeType.PESSIMISTIC_WRITE,
        FetchPaths.fromAttributeChain(Person_.organization));
```

### Collection associations

**JPA metamodel style** — pass the collection attribute followed by an attribute on the element type:

```java
// Fetch organization.persons and each person's role
Organization org = entityFinder.find(Organization.class, id,
        FetchPaths.fromAttributeChain(Organization_.persons, Person_.role));
```

`FetchPaths.fromAttributeChain` validates the chain at call time: if `Person_.role` is not declared on the element type of `Organization_.persons`, an `IllegalArgumentException` is thrown immediately.

**QueryDSL style** — paths can traverse collection associations using `.any()`:

```java
Organization org = entityFinder.find(Organization.class, id,
        QOrganization.organization.persons.any().role());
```

Both the `persons` collection and the `role` on each person are eagerly loaded. The path `persons.any().role()` implies `persons` is fetched — specifying `persons` separately would be redundant.
