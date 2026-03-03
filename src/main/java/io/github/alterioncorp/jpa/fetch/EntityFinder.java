package io.github.alterioncorp.jpa.fetch;

import java.util.Map;

import com.querydsl.core.types.Path;

import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQueryReference;

/**
 * Executes JPA queries with optional entity graph hints built from QueryDSL
 * path references.
 *
 * <p>Use {@link #createNamedQuery} or {@link #createQuery} to obtain a
 * {@link TypedFetchQuery}, then call {@link TypedFetchQuery#setFetchPaths} to
 * specify which associations to load eagerly before executing.
 */
public interface EntityFinder {

	/**
	 * Creates a {@link TypedFetchQuery} for the given named query.
	 *
	 * <p>Mirrors {@link jakarta.persistence.EntityManager#createNamedQuery(String, Class)}.
	 * Call {@link TypedFetchQuery#setFetchPaths} to specify associations to fetch
	 * eagerly before executing.
	 *
	 * @param <X>         the result type
	 * @param queryName   the name of the {@code @NamedQuery} to execute
	 * @param resultClass the expected result type
	 * @return a new {@link TypedFetchQuery} wrapping the named query
	 */
	<X> TypedFetchQuery<X> createNamedQuery(String queryName, Class<X> resultClass);

	/**
	 * Creates a {@link TypedFetchQuery} for the given JPQL string.
	 *
	 * <p>Mirrors {@link jakarta.persistence.EntityManager#createQuery(String, Class)}.
	 * Call {@link TypedFetchQuery#setFetchPaths} to specify associations to fetch
	 * eagerly before executing.
	 *
	 * @param <X>         the result type
	 * @param qlString    a JPQL query string
	 * @param resultClass the expected result type
	 * @return a new {@link TypedFetchQuery} wrapping the query
	 */
	<X> TypedFetchQuery<X> createQuery(String qlString, Class<X> resultClass);

	/**
	 * Creates a {@link TypedFetchQuery} for the given typed query reference.
	 *
	 * <p>Mirrors {@link jakarta.persistence.EntityManager#createQuery(jakarta.persistence.TypedQueryReference)}.
	 * Call {@link TypedFetchQuery#setFetchPaths} to specify associations to fetch
	 * eagerly before executing.
	 *
	 * @param <T>       the result type
	 * @param reference a typed reference to a named query
	 * @return a new {@link TypedFetchQuery} wrapping the named query
	 */
	<T> TypedFetchQuery<T> createQuery(TypedQueryReference<T> reference);

	/**
	 * Finds a single entity by its primary key.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param fetchPaths zero or more Q-type paths identifying associations to
	 *                   fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Object id, Path<?>... fetchPaths);

	/**
	 * Finds a single entity by its primary key, applying the given properties.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param properties standard and vendor-specific properties and hints
	 * @param fetchPaths zero or more Q-type paths identifying associations to
	 *                   fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Object id, Map<String, Object> properties, Path<?>... fetchPaths);

	/**
	 * Finds a single entity by its primary key with the given lock mode.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param lockMode   the lock mode to apply
	 * @param fetchPaths zero or more Q-type paths identifying associations to
	 *                   fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Object id, LockModeType lockMode, Path<?>... fetchPaths);

	/**
	 * Finds a single entity by its primary key with the given lock mode and
	 * properties.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param lockMode   the lock mode to apply
	 * @param properties standard and vendor-specific properties and hints
	 * @param fetchPaths zero or more Q-type paths identifying associations to
	 *                   fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Object id, LockModeType lockMode, Map<String, Object> properties, Path<?>... fetchPaths);

}
