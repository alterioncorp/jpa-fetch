package io.github.alterioncorp.jpa.fetch;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.github.alterioncorp.jpa.fetch.entities.Person;
import io.github.alterioncorp.jpa.fetch.entities.path.QPerson;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

public class EntityFinderImplMockTest {

	@SuppressWarnings("unchecked")
	private final TypedQuery<Person> typedQuery = Mockito.mock(TypedQuery.class);
	private final EntityManager entityManager = Mockito.mock(EntityManager.class);
	@SuppressWarnings("unchecked")
	private final EntityGraph<Person> entityGraph = Mockito.mock(EntityGraph.class);

	private EntityFinderImpl entityFinder;

	@BeforeEach
	public void setUp() {
		entityFinder = new EntityFinderImpl(entityManager);
	}

	// --- createNamedQuery ---

	@Test
	public void testCreateNamedQuery_ReturnsTypedFetchQuery() {
		Mockito.when(entityManager.createNamedQuery("Person.byName", Person.class)).thenReturn(typedQuery);

		TypedFetchQuery<Person> result = entityFinder.createNamedQuery("Person.byName", Person.class);

		Assertions.assertTrue(result instanceof TypedFetchQueryImpl);
	}

	// --- createQuery ---

	@Test
	public void testCreateQuery_ReturnsTypedFetchQuery() {
		Mockito.when(entityManager.createQuery("select e from Person e", Person.class)).thenReturn(typedQuery);

		TypedFetchQuery<Person> result = entityFinder.createQuery("select e from Person e", Person.class);

		Assertions.assertTrue(result instanceof TypedFetchQueryImpl);
	}

	// --- find(type, id, fetchPaths...) ---

	@Test
	public void testFind_ClearsBeforeFind() {
		InOrder inOrder = Mockito.inOrder(entityManager);

		entityFinder.find(Person.class, 1L);

		inOrder.verify(entityManager).clear();
		inOrder.verify(entityManager).find(Person.class, 1L, Map.of());
	}

	@Test
	public void testFind_NoPaths_PassesEmptyHints() {
		Person person = new Person("alice");
		Mockito.when(entityManager.find(Person.class, 1L, Map.of())).thenReturn(person);

		Person result = entityFinder.find(Person.class, 1L);

		Assertions.assertSame(person, result);
	}

	@Test
	public void testFind_WithPaths_AddsEntityGraphToHints() {
		Mockito.when(entityManager.createEntityGraph(Person.class)).thenReturn(entityGraph);

		entityFinder.find(Person.class, 1L, QPerson.person.organization());

		Mockito.verify(entityManager).find(Person.class, 1L,
				Map.of(EntityFinderImpl.HINT_FETCH_GRAPH, entityGraph));
	}

	// --- find(type, id, properties, fetchPaths...) ---

	@Test
	public void testFind_WithProperties_NoPaths() {
		Map<String, Object> properties = Map.of("someHint", "someValue");

		entityFinder.find(Person.class, 1L, properties);

		Mockito.verify(entityManager).find(Person.class, 1L, Map.of("someHint", "someValue"));
	}

	@Test
	public void testFind_WithPropertiesAndPaths_MergesBoth() {
		Map<String, Object> properties = Map.of("someHint", "someValue");
		Mockito.when(entityManager.createEntityGraph(Person.class)).thenReturn(entityGraph);

		entityFinder.find(Person.class, 1L, properties, QPerson.person.organization());

		Mockito.verify(entityManager).find(Person.class, 1L,
				Map.of("someHint", "someValue", EntityFinderImpl.HINT_FETCH_GRAPH, entityGraph));
	}

	// --- find(type, id, lockMode, fetchPaths...) ---

	@Test
	public void testFind_WithLockMode_NoPaths() {
		entityFinder.find(Person.class, 1L, LockModeType.PESSIMISTIC_WRITE);

		Mockito.verify(entityManager).clear();
		Mockito.verify(entityManager).find(Person.class, 1L, LockModeType.PESSIMISTIC_WRITE, Map.of());
	}

	@Test
	public void testFind_WithLockModeAndPaths_AddsEntityGraphToHints() {
		Mockito.when(entityManager.createEntityGraph(Person.class)).thenReturn(entityGraph);

		entityFinder.find(Person.class, 1L, LockModeType.PESSIMISTIC_WRITE, QPerson.person.organization());

		Mockito.verify(entityManager).find(Person.class, 1L, LockModeType.PESSIMISTIC_WRITE,
				Map.of(EntityFinderImpl.HINT_FETCH_GRAPH, entityGraph));
	}

	// --- find(type, id, lockMode, properties, fetchPaths...) ---

	@Test
	public void testFind_WithLockModeAndProperties_NoPaths() {
		Map<String, Object> properties = Map.of("someHint", "someValue");

		entityFinder.find(Person.class, 1L, LockModeType.PESSIMISTIC_READ, properties);

		Mockito.verify(entityManager).find(Person.class, 1L, LockModeType.PESSIMISTIC_READ,
				Map.of("someHint", "someValue"));
	}

	@Test
	public void testFind_WithLockModePropertiesAndPaths_MergesAll() {
		Map<String, Object> properties = Map.of("someHint", "someValue");
		Mockito.when(entityManager.createEntityGraph(Person.class)).thenReturn(entityGraph);

		entityFinder.find(Person.class, 1L, LockModeType.PESSIMISTIC_READ, properties, QPerson.person.organization());

		Mockito.verify(entityManager).find(Person.class, 1L, LockModeType.PESSIMISTIC_READ,
				Map.of("someHint", "someValue", EntityFinderImpl.HINT_FETCH_GRAPH, entityGraph));
	}
}
