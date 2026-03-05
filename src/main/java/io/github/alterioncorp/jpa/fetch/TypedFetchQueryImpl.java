package io.github.alterioncorp.jpa.fetch;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

/**
 * Package-private implementation of {@link TypedFetchQuery}.
 *
 * <p>Wraps a provider {@link TypedQuery} and forwards all calls to it.
 * {@link #setFetchPaths(FetchPath...)} builds an {@code EntityGraph} from the given
 * paths and applies it as a {@code jakarta.persistence.fetchgraph} hint on the delegate.
 * {@link #setFetchPaths(com.querydsl.core.types.Path[])} converts QueryDSL paths via
 * {@link FetchPaths#fromQueryDsl} before delegating.
 *
 * @param <X> the query result type
 */
class TypedFetchQueryImpl<X> implements TypedFetchQuery<X> {

	private final EntityManager entityManager;
	private final TypedQuery<X> delegate;
	private final Class<X> type;

	TypedFetchQueryImpl(EntityManager entityManager, TypedQuery<X> delegate, Class<X> type) {
		this.entityManager = entityManager;
		this.delegate = delegate;
		this.type = type;
	}

	@Override
	public TypedFetchQuery<X> setFetchPaths(FetchPath... fetchPaths) {
		if (fetchPaths.length > 0) {
			EntityGraph<X> graph = entityManager.createEntityGraph(type);
			PathParser.buildTree(fetchPaths).addToGraph(graph);
			delegate.setHint(EntityFinderImpl.HINT_FETCH_GRAPH, graph);
		}
		return this;
	}

	@Override
	public List<X> getResultList() {
		return delegate.getResultList();
	}

	@Override
	public Stream<X> getResultStream() {
		return delegate.getResultStream();
	}

	@Override
	public X getSingleResult() {
		return delegate.getSingleResult();
	}

	@Override
	public X getSingleResultOrNull() {
		return delegate.getSingleResultOrNull();
	}

	@Override
	public int executeUpdate() {
		return delegate.executeUpdate();
	}

	@Override
	public TypedFetchQuery<X> setMaxResults(int maxResult) {
		delegate.setMaxResults(maxResult);
		return this;
	}

	@Override
	public int getMaxResults() {
		return delegate.getMaxResults();
	}

	@Override
	public TypedFetchQuery<X> setFirstResult(int startPosition) {
		delegate.setFirstResult(startPosition);
		return this;
	}

	@Override
	public int getFirstResult() {
		return delegate.getFirstResult();
	}

	@Override
	public TypedFetchQuery<X> setHint(String hintName, Object value) {
		delegate.setHint(hintName, value);
		return this;
	}

	@Override
	public Map<String, Object> getHints() {
		return delegate.getHints();
	}

	@Override
	public <T> TypedFetchQuery<X> setParameter(Parameter<T> param, T value) {
		delegate.setParameter(param, value);
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public TypedFetchQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
		delegate.setParameter(param, value, temporalType);
		return this;
	}

	@Override
	@SuppressWarnings("deprecation")
	public TypedFetchQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
		delegate.setParameter(param, value, temporalType);
		return this;
	}

	@Override
	public TypedFetchQuery<X> setParameter(String name, Object value) {
		delegate.setParameter(name, value);
		return this;
	}

	@Override
	@SuppressWarnings("deprecation")
	public TypedFetchQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
		delegate.setParameter(name, value, temporalType);
		return this;
	}

	@Override
	@SuppressWarnings("deprecation")
	public TypedFetchQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
		delegate.setParameter(name, value, temporalType);
		return this;
	}

	@Override
	public TypedFetchQuery<X> setParameter(int position, Object value) {
		delegate.setParameter(position, value);
		return this;
	}

	@Override
	@SuppressWarnings("deprecation")
	public TypedFetchQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
		delegate.setParameter(position, value, temporalType);
		return this;
	}

	@Override
	@SuppressWarnings("deprecation")
	public TypedFetchQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
		delegate.setParameter(position, value, temporalType);
		return this;
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return delegate.getParameters();
	}

	@Override
	public Parameter<?> getParameter(String name) {
		return delegate.getParameter(name);
	}

	@Override
	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		return delegate.getParameter(name, type);
	}

	@Override
	public Parameter<?> getParameter(int position) {
		return delegate.getParameter(position);
	}

	@Override
	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		return delegate.getParameter(position, type);
	}

	@Override
	public boolean isBound(Parameter<?> param) {
		return delegate.isBound(param);
	}

	@Override
	public <T> T getParameterValue(Parameter<T> param) {
		return delegate.getParameterValue(param);
	}

	@Override
	public Object getParameterValue(String name) {
		return delegate.getParameterValue(name);
	}

	@Override
	public Object getParameterValue(int position) {
		return delegate.getParameterValue(position);
	}

	@Override
	public TypedFetchQuery<X> setFlushMode(FlushModeType flushMode) {
		delegate.setFlushMode(flushMode);
		return this;
	}

	@Override
	public FlushModeType getFlushMode() {
		return delegate.getFlushMode();
	}

	@Override
	public TypedFetchQuery<X> setLockMode(LockModeType lockMode) {
		delegate.setLockMode(lockMode);
		return this;
	}

	@Override
	public LockModeType getLockMode() {
		return delegate.getLockMode();
	}

	@Override
	public TypedFetchQuery<X> setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
		delegate.setCacheRetrieveMode(cacheRetrieveMode);
		return this;
	}

	@Override
	public CacheRetrieveMode getCacheRetrieveMode() {
		return delegate.getCacheRetrieveMode();
	}

	@Override
	public TypedFetchQuery<X> setCacheStoreMode(CacheStoreMode cacheStoreMode) {
		delegate.setCacheStoreMode(cacheStoreMode);
		return this;
	}

	@Override
	public CacheStoreMode getCacheStoreMode() {
		return delegate.getCacheStoreMode();
	}

	@Override
	public TypedFetchQuery<X> setTimeout(Integer timeout) {
		delegate.setTimeout(timeout);
		return this;
	}

	@Override
	public Integer getTimeout() {
		return delegate.getTimeout();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		if (cls.isAssignableFrom(this.getClass())) {
			return cls.cast(this);
		}
		return delegate.unwrap(cls);
	}
}
