package io.github.alterioncorp.jpa.fetch;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.alterioncorp.jpa.fetch.entities.Person;
import io.github.alterioncorp.jpa.fetch.entities.path.QPerson;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TypedQuery;

public class TypedFetchQueryImplTest {

	@SuppressWarnings("unchecked")
	private final TypedQuery<Person> delegate = Mockito.mock(TypedQuery.class);
	private final EntityManager entityManager = Mockito.mock(EntityManager.class);
	@SuppressWarnings("unchecked")
	private final EntityGraph<Person> entityGraph = Mockito.mock(EntityGraph.class);
	@SuppressWarnings("rawtypes")
	private final Subgraph subgraph = Mockito.mock(Subgraph.class);

	private TypedFetchQueryImpl<Person> query;

	@BeforeEach
	public void setUp() {
		query = new TypedFetchQueryImpl<>(entityManager, delegate, Person.class);
	}

	@Test
	public void testSetFetchPaths_NoPaths() {
		TypedFetchQuery<Person> result = query.setFetchPaths();

		Mockito.verifyNoInteractions(entityManager);
		Mockito.verify(delegate, Mockito.never()).setHint(Mockito.anyString(), Mockito.any());
		Assertions.assertSame(query, result);
	}

	@Test
	public void testSetFetchPaths_WithPath() {
		Mockito.when(entityManager.createEntityGraph(Person.class)).thenReturn(entityGraph);

		TypedFetchQuery<Person> result = query.setFetchPaths(QPerson.person.organization());

		Mockito.verify(entityGraph).addAttributeNodes("organization");
		Mockito.verify(delegate).setHint(EntityFinderImpl.HINT_FETCH_GRAPH, entityGraph);
		Assertions.assertSame(query, result);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSetFetchPaths_WithNestedPath() {
		Mockito.when(entityManager.createEntityGraph(Person.class)).thenReturn(entityGraph);
		Mockito.when(entityGraph.addSubgraph("organization")).thenReturn(subgraph);

		query.setFetchPaths(QPerson.person.organization().country());

		Mockito.verify(entityGraph).addSubgraph("organization");
		Mockito.verify(subgraph).addAttributeNodes("country");
		Mockito.verify(delegate).setHint(EntityFinderImpl.HINT_FETCH_GRAPH, entityGraph);
	}

	@Test
	public void testGetResultList_Delegates() {
		Person person = new Person("alice");
		Mockito.when(delegate.getResultList()).thenReturn(List.of(person));

		Assertions.assertSame(person, query.getResultList().get(0));
	}

	@Test
	public void testGetSingleResultOrNull_Delegates() {
		Person person = new Person("alice");
		Mockito.when(delegate.getSingleResultOrNull()).thenReturn(person);

		Assertions.assertSame(person, query.getSingleResultOrNull());
	}

	@Test
	public void testExecuteUpdate_Delegates() {
		Mockito.when(delegate.executeUpdate()).thenReturn(3);

		Assertions.assertEquals(3, query.executeUpdate());
	}

	@Test
	public void testGetMaxResults_Delegates() {
		Mockito.when(delegate.getMaxResults()).thenReturn(50);

		Assertions.assertEquals(50, query.getMaxResults());
	}

	@Test
	public void testGetHints_Delegates() {
		Map<String, Object> hints = Map.of("key", "value");
		Mockito.when(delegate.getHints()).thenReturn(hints);

		Assertions.assertSame(hints, query.getHints());
	}

	@Test
	public void testGetFlushMode_Delegates() {
		Mockito.when(delegate.getFlushMode()).thenReturn(FlushModeType.COMMIT);

		Assertions.assertEquals(FlushModeType.COMMIT, query.getFlushMode());
	}

	@Test
	public void testSetMaxResults_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setMaxResults(10);

		Mockito.verify(delegate).setMaxResults(10);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testSetFirstResult_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setFirstResult(5);

		Mockito.verify(delegate).setFirstResult(5);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testSetHint_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setHint("myHint", "value");

		Mockito.verify(delegate).setHint("myHint", "value");
		Assertions.assertSame(query, result);
	}

	@Test
	public void testSetParameterByPosition_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setParameter(1, "value");

		Mockito.verify(delegate).setParameter(1, "value");
		Assertions.assertSame(query, result);
	}

	@Test
	public void testSetParameterByName_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setParameter("name", "value");

		Mockito.verify(delegate).setParameter("name", "value");
		Assertions.assertSame(query, result);
	}

	@Test
	public void testSetFlushMode_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setFlushMode(FlushModeType.COMMIT);

		Mockito.verify(delegate).setFlushMode(FlushModeType.COMMIT);
		Assertions.assertSame(query, result);
	}
}
