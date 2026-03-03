package io.github.alterioncorp.jpa.fetch;

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

public interface TypedFetchQuery<X> extends TypedQuery<X> {

	TypedFetchQuery<X> setFetchPaths(Path<?>... fetchPaths);

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
