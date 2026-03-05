package io.github.alterioncorp.jpa.fetch;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.querydsl.core.types.Path;

import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

/**
 * A {@link TypedQuery} extension that adds fetch-path control as a first-class concern.
 * Callers specify which associations to eagerly load via {@link #setFetchPaths}, which
 * builds a JPA {@code EntityGraph} from the supplied paths and applies it as a
 * {@code jakarta.persistence.fetchgraph} hint. All mutating {@code TypedQuery} methods are
 * overridden with covariant return types to support fluent chaining.
 *
 * @param <X> the query result type
 */
public interface TypedFetchQuery<X> extends TypedQuery<X> {

	/**
	 * Builds a JPA {@code EntityGraph} from the given fetch paths and applies it as a
	 * {@code jakarta.persistence.fetchgraph} fetch hint on this query.
	 *
	 * @param fetchPaths fetch paths identifying the associations to eagerly load
	 * @return this query (for fluent chaining)
	 */
	TypedFetchQuery<X> setFetchPaths(FetchPath... fetchPaths);

	/**
	 * Builds a JPA {@code EntityGraph} from the given QueryDSL paths and applies it as a
	 * {@code jakarta.persistence.fetchgraph} fetch hint on this query.
	 *
	 * @param fetchPaths QueryDSL {@code Path} values identifying the associations to eagerly load
	 * @return this query (for fluent chaining)
	 */
	default TypedFetchQuery<X> setFetchPaths(Path<?>... fetchPaths) {
		return setFetchPaths(Arrays.stream(fetchPaths).map(FetchPaths::of).toArray(FetchPath[]::new));
	}

	@Override
	TypedFetchQuery<X> setMaxResults(int maxResult);
	@Override
	TypedFetchQuery<X> setFirstResult(int startPosition);
	@Override
	TypedFetchQuery<X> setHint(String hintName, Object value);
	@Override
	TypedFetchQuery<X> setFlushMode(FlushModeType flushMode);
	@Override
	TypedFetchQuery<X> setLockMode(LockModeType lockMode);
	@Override
	TypedFetchQuery<X> setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode);
	@Override
	TypedFetchQuery<X> setCacheStoreMode(CacheStoreMode cacheStoreMode);
	@Override
	TypedFetchQuery<X> setTimeout(Integer timeout);

	@Override
	<T> TypedFetchQuery<X> setParameter(Parameter<T> param, T value);
	@SuppressWarnings("deprecation")
	@Override
	TypedFetchQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType);
	@SuppressWarnings("deprecation")
	@Override
	TypedFetchQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType);
	@Override
	TypedFetchQuery<X> setParameter(String name, Object value);
	@SuppressWarnings("deprecation")
	@Override
	TypedFetchQuery<X> setParameter(String name, Calendar value, TemporalType temporalType);
	@SuppressWarnings("deprecation")
	@Override
	TypedFetchQuery<X> setParameter(String name, Date value, TemporalType temporalType);
	@Override
	TypedFetchQuery<X> setParameter(int position, Object value);
	@SuppressWarnings("deprecation")
	@Override
	TypedFetchQuery<X> setParameter(int position, Calendar value, TemporalType temporalType);
	@SuppressWarnings("deprecation")
	@Override
	TypedFetchQuery<X> setParameter(int position, Date value, TemporalType temporalType);
}
