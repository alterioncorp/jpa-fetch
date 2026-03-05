package io.github.alterioncorp.jpa.fetch;

import java.util.Arrays;
import java.util.Map;

import com.querydsl.core.types.Path;

import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQueryReference;

/**
 * Executes JPA queries with fetch control as a first-class concern.
 *
 * <p>Use {@link #createNamedQuery} or {@link #createQuery} to obtain a
 * {@link TypedFetchQuery}, then call {@link TypedFetchQuery#setFetchPaths} to
 * specify which associations to load eagerly before executing.
 *
 * <p>The {@code find} methods accept either {@link FetchPath} instances (created via
 * {@link FetchPaths}) or QueryDSL {@code Path<?>} references, both of which are
 * translated into a JPA {@code EntityGraph} at runtime.
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

	// --- find with no fetch paths ---

	/**
	 * Finds a single entity by its primary key with no eager fetching.
	 *
	 * @param <T>  the entity type
	 * @param type the entity class
	 * @param id   the primary key value
	 * @return the matching entity, or {@code null} if not found
	 */
	default <T> T find(Class<T> type, Object id) {
		return find(type, id, new FetchPath[0]);
	}

	/**
	 * Finds a single entity by its primary key with no eager fetching, applying the given properties.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param properties standard and vendor-specific properties and hints
	 * @return the matching entity, or {@code null} if not found
	 */
	default <T> T find(Class<T> type, Object id, Map<String, Object> properties) {
		return find(type, id, properties, new FetchPath[0]);
	}

	/**
	 * Finds a single entity by its primary key with no eager fetching and the given lock mode.
	 *
	 * @param <T>      the entity type
	 * @param type     the entity class
	 * @param id       the primary key value
	 * @param lockMode the lock mode to apply
	 * @return the matching entity, or {@code null} if not found
	 */
	default <T> T find(Class<T> type, Object id, LockModeType lockMode) {
		return find(type, id, lockMode, new FetchPath[0]);
	}

	/**
	 * Finds a single entity by its primary key with no eager fetching, the given lock mode and properties.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param lockMode   the lock mode to apply
	 * @param properties standard and vendor-specific properties and hints
	 * @return the matching entity, or {@code null} if not found
	 */
	default <T> T find(Class<T> type, Object id, LockModeType lockMode, Map<String, Object> properties) {
		return find(type, id, lockMode, properties, new FetchPath[0]);
	}

	// --- find with FetchPath varargs ---

	/**
	 * Finds a single entity by its primary key.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param fetchPaths one or more fetch paths identifying associations to fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Object id, FetchPath... fetchPaths);

	/**
	 * Finds a single entity by its primary key, applying the given properties.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param properties standard and vendor-specific properties and hints
	 * @param fetchPaths one or more fetch paths identifying associations to fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Object id, Map<String, Object> properties, FetchPath... fetchPaths);

	/**
	 * Finds a single entity by its primary key with the given lock mode.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param lockMode   the lock mode to apply
	 * @param fetchPaths one or more fetch paths identifying associations to fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Object id, LockModeType lockMode, FetchPath... fetchPaths);

	/**
	 * Finds a single entity by its primary key with the given lock mode and properties.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param lockMode   the lock mode to apply
	 * @param properties standard and vendor-specific properties and hints
	 * @param fetchPaths one or more fetch paths identifying associations to fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	<T> T find(Class<T> type, Object id, LockModeType lockMode, Map<String, Object> properties, FetchPath... fetchPaths);

	// --- find with QueryDSL Path varargs ---

	/**
	 * Finds a single entity by its primary key.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param fetchPaths one or more Q-type paths identifying associations to fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	default <T> T find(Class<T> type, Object id, Path<?>... fetchPaths) {
		return find(type, id, toFetchPaths(fetchPaths));
	}

	/**
	 * Finds a single entity by its primary key, applying the given properties.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param properties standard and vendor-specific properties and hints
	 * @param fetchPaths one or more Q-type paths identifying associations to fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	default <T> T find(Class<T> type, Object id, Map<String, Object> properties, Path<?>... fetchPaths) {
		return find(type, id, properties, toFetchPaths(fetchPaths));
	}

	/**
	 * Finds a single entity by its primary key with the given lock mode.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param lockMode   the lock mode to apply
	 * @param fetchPaths one or more Q-type paths identifying associations to fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	default <T> T find(Class<T> type, Object id, LockModeType lockMode, Path<?>... fetchPaths) {
		return find(type, id, lockMode, toFetchPaths(fetchPaths));
	}

	/**
	 * Finds a single entity by its primary key with the given lock mode and
	 * properties.
	 *
	 * @param <T>        the entity type
	 * @param type       the entity class
	 * @param id         the primary key value
	 * @param lockMode   the lock mode to apply
	 * @param properties standard and vendor-specific properties and hints
	 * @param fetchPaths one or more Q-type paths identifying associations to fetch eagerly
	 * @return the matching entity, or {@code null} if not found
	 */
	default <T> T find(Class<T> type, Object id, LockModeType lockMode, Map<String, Object> properties, Path<?>... fetchPaths) {
		return find(type, id, lockMode, properties, toFetchPaths(fetchPaths));
	}

	private static FetchPath[] toFetchPaths(Path<?>... paths) {
		return Arrays.stream(paths).map(FetchPaths::fromQueryDsl).toArray(FetchPath[]::new);
	}

	/**
	 * Clears the persistence context, causing all managed entities to become
	 * detached.
	 *
	 * <p>Mirrors {@link jakarta.persistence.EntityManager#clear()}.
	 */
	void clear();

}
