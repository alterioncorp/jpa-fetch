package io.github.alterioncorp.jpa.fetch;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.alterioncorp.jpa.fetch.entities.Person;
import io.github.alterioncorp.jpa.fetch.entities.path.QPerson;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TemporalType;
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

	// --- setFetchPaths ---

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

	// --- query execution ---

	@Test
	public void testGetResultList_Delegates() {
		Person person = new Person("alice");
		Mockito.when(delegate.getResultList()).thenReturn(List.of(person));

		Assertions.assertSame(person, query.getResultList().get(0));
	}

	@Test
	public void testGetResultStream_Delegates() {
		Stream<Person> stream = Stream.of(new Person("alice"));
		Mockito.when(delegate.getResultStream()).thenReturn(stream);

		Assertions.assertSame(stream, query.getResultStream());
	}

	@Test
	public void testGetSingleResult_Delegates() {
		Person person = new Person("alice");
		Mockito.when(delegate.getSingleResult()).thenReturn(person);

		Assertions.assertSame(person, query.getSingleResult());
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

	// --- pagination ---

	@Test
	public void testSetMaxResults_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setMaxResults(10);

		Mockito.verify(delegate).setMaxResults(10);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testGetMaxResults_Delegates() {
		Mockito.when(delegate.getMaxResults()).thenReturn(50);

		Assertions.assertEquals(50, query.getMaxResults());
	}

	@Test
	public void testSetFirstResult_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setFirstResult(5);

		Mockito.verify(delegate).setFirstResult(5);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testGetFirstResult_Delegates() {
		Mockito.when(delegate.getFirstResult()).thenReturn(5);

		Assertions.assertEquals(5, query.getFirstResult());
	}

	// --- hints ---

	@Test
	public void testSetHint_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setHint("myHint", "value");

		Mockito.verify(delegate).setHint("myHint", "value");
		Assertions.assertSame(query, result);
	}

	@Test
	public void testGetHints_Delegates() {
		Map<String, Object> hints = Map.of("key", "value");
		Mockito.when(delegate.getHints()).thenReturn(hints);

		Assertions.assertSame(hints, query.getHints());
	}

	// --- setParameter ---

	@Test
	public void testSetParameterByParameter_DelegatesAndReturnsThis() {
		@SuppressWarnings("unchecked")
		Parameter<String> param = Mockito.mock(Parameter.class);

		TypedFetchQuery<Person> result = query.setParameter(param, "value");

		Mockito.verify(delegate).setParameter(param, "value");
		Assertions.assertSame(query, result);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSetParameterByCalendarParameter_DelegatesAndReturnsThis() {
		@SuppressWarnings("unchecked")
		Parameter<Calendar> param = Mockito.mock(Parameter.class);
		Calendar calendar = Calendar.getInstance();

		TypedFetchQuery<Person> result = query.setParameter(param, calendar, TemporalType.DATE);

		Mockito.verify(delegate).setParameter(param, calendar, TemporalType.DATE);
		Assertions.assertSame(query, result);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSetParameterByDateParameter_DelegatesAndReturnsThis() {
		@SuppressWarnings("unchecked")
		Parameter<Date> param = Mockito.mock(Parameter.class);
		Date date = new Date();

		TypedFetchQuery<Person> result = query.setParameter(param, date, TemporalType.DATE);

		Mockito.verify(delegate).setParameter(param, date, TemporalType.DATE);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testSetParameterByName_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setParameter("name", "value");

		Mockito.verify(delegate).setParameter("name", "value");
		Assertions.assertSame(query, result);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSetParameterByNameCalendar_DelegatesAndReturnsThis() {
		Calendar calendar = Calendar.getInstance();

		TypedFetchQuery<Person> result = query.setParameter("date", calendar, TemporalType.DATE);

		Mockito.verify(delegate).setParameter("date", calendar, TemporalType.DATE);
		Assertions.assertSame(query, result);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSetParameterByNameDate_DelegatesAndReturnsThis() {
		Date date = new Date();

		TypedFetchQuery<Person> result = query.setParameter("date", date, TemporalType.DATE);

		Mockito.verify(delegate).setParameter("date", date, TemporalType.DATE);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testSetParameterByPosition_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setParameter(1, "value");

		Mockito.verify(delegate).setParameter(1, "value");
		Assertions.assertSame(query, result);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSetParameterByPositionCalendar_DelegatesAndReturnsThis() {
		Calendar calendar = Calendar.getInstance();

		TypedFetchQuery<Person> result = query.setParameter(1, calendar, TemporalType.DATE);

		Mockito.verify(delegate).setParameter(1, calendar, TemporalType.DATE);
		Assertions.assertSame(query, result);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSetParameterByPositionDate_DelegatesAndReturnsThis() {
		Date date = new Date();

		TypedFetchQuery<Person> result = query.setParameter(1, date, TemporalType.DATE);

		Mockito.verify(delegate).setParameter(1, date, TemporalType.DATE);
		Assertions.assertSame(query, result);
	}

	// --- getParameter / getParameters ---

	@Test
	public void testGetParameters_Delegates() {
		Set<Parameter<?>> params = Set.of();
		Mockito.when(delegate.getParameters()).thenReturn(params);

		Assertions.assertSame(params, query.getParameters());
	}

	@Test
	public void testGetParameterByName_Delegates() {
		Parameter<?> param = Mockito.mock(Parameter.class);
		Mockito.doReturn(param).when(delegate).getParameter("name");

		Assertions.assertSame(param, query.getParameter("name"));
	}

	@Test
	public void testGetParameterByNameAndType_Delegates() {
		@SuppressWarnings("unchecked")
		Parameter<String> param = Mockito.mock(Parameter.class);
		Mockito.when(delegate.getParameter("name", String.class)).thenReturn(param);

		Assertions.assertSame(param, query.getParameter("name", String.class));
	}

	@Test
	public void testGetParameterByPosition_Delegates() {
		Parameter<?> param = Mockito.mock(Parameter.class);
		Mockito.doReturn(param).when(delegate).getParameter(1);

		Assertions.assertSame(param, query.getParameter(1));
	}

	@Test
	public void testGetParameterByPositionAndType_Delegates() {
		@SuppressWarnings("unchecked")
		Parameter<String> param = Mockito.mock(Parameter.class);
		Mockito.when(delegate.getParameter(1, String.class)).thenReturn(param);

		Assertions.assertSame(param, query.getParameter(1, String.class));
	}

	@Test
	public void testIsBound_Delegates() {
		Parameter<?> param = Mockito.mock(Parameter.class);
		Mockito.when(delegate.isBound(param)).thenReturn(true);

		Assertions.assertTrue(query.isBound(param));
	}

	// --- getParameterValue ---

	@Test
	public void testGetParameterValueByParameter_Delegates() {
		@SuppressWarnings("unchecked")
		Parameter<String> param = Mockito.mock(Parameter.class);
		Mockito.when(delegate.getParameterValue(param)).thenReturn("value");

		Assertions.assertEquals("value", query.getParameterValue(param));
	}

	@Test
	public void testGetParameterValueByName_Delegates() {
		Mockito.when(delegate.getParameterValue("name")).thenReturn("value");

		Assertions.assertEquals("value", query.getParameterValue("name"));
	}

	@Test
	public void testGetParameterValueByPosition_Delegates() {
		Mockito.when(delegate.getParameterValue(1)).thenReturn("value");

		Assertions.assertEquals("value", query.getParameterValue(1));
	}

	// --- flush mode ---

	@Test
	public void testSetFlushMode_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setFlushMode(FlushModeType.COMMIT);

		Mockito.verify(delegate).setFlushMode(FlushModeType.COMMIT);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testGetFlushMode_Delegates() {
		Mockito.when(delegate.getFlushMode()).thenReturn(FlushModeType.COMMIT);

		Assertions.assertEquals(FlushModeType.COMMIT, query.getFlushMode());
	}

	// --- lock mode ---

	@Test
	public void testSetLockMode_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setLockMode(LockModeType.PESSIMISTIC_READ);

		Mockito.verify(delegate).setLockMode(LockModeType.PESSIMISTIC_READ);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testGetLockMode_Delegates() {
		Mockito.when(delegate.getLockMode()).thenReturn(LockModeType.PESSIMISTIC_READ);

		Assertions.assertEquals(LockModeType.PESSIMISTIC_READ, query.getLockMode());
	}

	// --- cache modes ---

	@Test
	public void testSetCacheRetrieveMode_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setCacheRetrieveMode(CacheRetrieveMode.USE);

		Mockito.verify(delegate).setCacheRetrieveMode(CacheRetrieveMode.USE);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testGetCacheRetrieveMode_Delegates() {
		Mockito.when(delegate.getCacheRetrieveMode()).thenReturn(CacheRetrieveMode.USE);

		Assertions.assertEquals(CacheRetrieveMode.USE, query.getCacheRetrieveMode());
	}

	@Test
	public void testSetCacheStoreMode_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setCacheStoreMode(CacheStoreMode.USE);

		Mockito.verify(delegate).setCacheStoreMode(CacheStoreMode.USE);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testGetCacheStoreMode_Delegates() {
		Mockito.when(delegate.getCacheStoreMode()).thenReturn(CacheStoreMode.USE);

		Assertions.assertEquals(CacheStoreMode.USE, query.getCacheStoreMode());
	}

	// --- timeout ---

	@Test
	public void testSetTimeout_DelegatesAndReturnsThis() {
		TypedFetchQuery<Person> result = query.setTimeout(30);

		Mockito.verify(delegate).setTimeout(30);
		Assertions.assertSame(query, result);
	}

	@Test
	public void testGetTimeout_Delegates() {
		Mockito.when(delegate.getTimeout()).thenReturn(30);

		Assertions.assertEquals(30, query.getTimeout());
	}

	// --- unwrap ---

	@Test
	public void testUnwrap_ReturnsSelfForOwnType() {
		Assertions.assertSame(query, query.unwrap(TypedFetchQueryImpl.class));
		Assertions.assertSame(query, query.unwrap(TypedFetchQuery.class));
		Mockito.verifyNoInteractions(delegate);
	}

	@Test
	public void testUnwrap_DelegatesToProviderForUnknownType() {
		Mockito.when(delegate.unwrap(String.class)).thenReturn("unwrapped");

		Assertions.assertEquals("unwrapped", query.unwrap(String.class));
	}
}
