package io.github.alterioncorp.jpa.fetch;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.EntityGraph;

/**
 * The root of a path tree, holding a set of top-level {@link PathNode}s that
 * together describe which associations to include in a JPA {@link EntityGraph}.
 *
 * <p>When a node is {@link #addNode added}, it is merged into any existing root
 * node with the same attribute name, so that overlapping paths share a common
 * prefix rather than duplicating subgraph nodes.
 */
class PathTree {

	private final Set<PathNode> roots = new HashSet<>();

	/**
	 * Applies all root nodes to the given {@link EntityGraph}, producing the
	 * attribute nodes and subgraphs that correspond to the paths held in this tree.
	 *
	 * @param entityGraph the graph to populate
	 */
	public void addToGraph(EntityGraph<?> entityGraph) {
		roots.forEach(root -> root.addToGraph(entityGraph));
	}

	/**
	 * Returns the set of root nodes.
	 *
	 * @return the root nodes, never {@code null}
	 */
	public Set<PathNode> getRoots() {
		return roots;
	}

	/**
	 * Returns the root node with the given attribute name, or {@code null} if no
	 * such node exists.
	 *
	 * @param value the attribute name to look up
	 * @return the matching root node, or {@code null}
	 */
	public PathNode getRoot(String value) {
		return roots.stream()
				.filter(root -> root.getValue().equals(value))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Adds a root node to this tree, merging it with an existing node of the same
	 * attribute name if one is present.
	 *
	 * @param node the node to add or merge
	 */
	public void addNode(PathNode node) {
		boolean merged = roots.stream().anyMatch(root -> root.merge(node));
		if (! merged) {
			roots.add(node);
		}
	}
}
