package io.github.alterioncorp.jpa.fetch;

/**
 * Represents a single fetch path as an ordered array of JPA attribute name segments.
 *
 * <p>The segments correspond to the association chain to eagerly load — for example,
 * {@code ["organization", "country"]} causes both the {@code organization} association
 * and its nested {@code country} association to be fetched.
 *
 * <p>Instances are typically obtained via {@link FetchPaths} rather than implemented directly.
 */
@FunctionalInterface
public interface FetchPath {

	/**
	 * Returns the JPA attribute name segments that make up this path, in order from
	 * the root entity outward.
	 *
	 * @return the path segments; never {@code null}
	 */
	String[] segments();
}
