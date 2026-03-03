package io.github.alterioncorp.jpa.fetch;

import java.util.HashMap;
import java.util.Map;

import com.querydsl.core.types.Path;

import jakarta.persistence.LockModeType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.TypedQueryReference;

/**
 * CDI-managed implementation of {@link EntityFinder}.
 *
 * <p>Bound to the {@code "default"} persistence unit via
 * {@link jakarta.persistence.PersistenceContext}. Declare it as an injection
 * point in any CDI bean:
 *
 * <pre>{@code
 * @Inject
 * EntityFinder entityFinder;
 * }</pre>
 *
 * <p>Entity graphs are constructed from the supplied paths and applied as a
 * {@code jakarta.persistence.fetchgraph} hint, so only the attributes
 * reachable via those paths are eagerly loaded.
 *
 */
@ApplicationScoped
public class EntityFinderImpl implements EntityFinder {

	static final String HINT_FETCH_GRAPH = "jakarta.persistence.fetchgraph";

	@PersistenceContext(unitName = "default")
	EntityManager entityManager;

	/** No-arg constructor for CDI. */
	public EntityFinderImpl() {}

	/**
	 * Constructor for use in tests or where CDI is not available.
	 *
	 * @param entityManager the entity manager to use
	 */
	public EntityFinderImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/** {@inheritDoc} */
	@Override
	public <X> TypedFetchQuery<X> createNamedQuery(String queryName, Class<X> resultClass) {
		TypedQuery<X> query = entityManager.createNamedQuery(queryName, resultClass);
		return new TypedFetchQueryImpl<>(entityManager, query, resultClass);
	}

	/** {@inheritDoc} */
	@Override
	public <X> TypedFetchQuery<X> createQuery(String qlString, Class<X> resultClass) {
		TypedQuery<X> query = entityManager.createQuery(qlString, resultClass);
		return new TypedFetchQueryImpl<>(entityManager, query, resultClass);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public <T> TypedFetchQuery<T> createQuery(TypedQueryReference<T> reference) {
		TypedQuery<T> query = entityManager.createQuery(reference);
		return new TypedFetchQueryImpl<>(entityManager, query, (Class<T>) reference.getResultType());
	}

	/** {@inheritDoc} */
	@Override
	public <T> T find(Class<T> type, Object id, Path<?>... fetchPaths) {
		entityManager.clear();
		return entityManager.find(type, id, buildHints(type, Map.of(), fetchPaths));
	}

	/** {@inheritDoc} */
	@Override
	public <T> T find(Class<T> type, Object id, Map<String, Object> properties, Path<?>... fetchPaths) {
		entityManager.clear();
		return entityManager.find(type, id, buildHints(type, properties, fetchPaths));
	}

	/** {@inheritDoc} */
	@Override
	public <T> T find(Class<T> type, Object id, LockModeType lockMode, Path<?>... fetchPaths) {
		entityManager.clear();
		return entityManager.find(type, id, lockMode, buildHints(type, Map.of(), fetchPaths));
	}

	/** {@inheritDoc} */
	@Override
	public <T> T find(Class<T> type, Object id, LockModeType lockMode, Map<String, Object> properties, Path<?>... fetchPaths) {
		entityManager.clear();
		return entityManager.find(type, id, lockMode, buildHints(type, properties, fetchPaths));
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		entityManager.clear();
	}

	private <T> Map<String, Object> buildHints(Class<T> type, Map<String, Object> base, Path<?>... fetchPaths) {
		HashMap<String, Object> hints = new HashMap<>(base);
		if (fetchPaths.length > 0) {
			hints.put(HINT_FETCH_GRAPH, createEntityGraph(type, fetchPaths));
		}
		return hints;
	}

	private <T> EntityGraph<T> createEntityGraph(Class<T> type, Path<?>... fetchPaths) {
		EntityGraph<T> entityGraph = entityManager.createEntityGraph(type);
		PathParser.buildTree(fetchPaths).addToGraph(entityGraph);
		return entityGraph;
	}
}
