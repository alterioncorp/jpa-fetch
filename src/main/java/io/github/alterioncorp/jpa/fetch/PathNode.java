package io.github.alterioncorp.jpa.fetch;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Graph;
import jakarta.persistence.Subgraph;

/**
 * A node in a path tree, representing a single JPA attribute name.
 *
 * <p>Nodes form a tree where each level corresponds to one hop in an association
 * chain. A node with no children is a leaf and maps to an
 * {@link Graph#addAttributeNodes attribute node} in the entity graph. A
 * node with children maps to a {@link Graph#addSubgraph subgraph}, with
 * its children applied recursively.
 *
 * <p>Equality and hashing are based solely on {@link #getValue() value}, so two
 * nodes with the same attribute name are considered equal regardless of their
 * children. This allows {@link #merge(PathNode)} to combine paths that
 * share a common prefix into a single subgraph node.
 */
class PathNode {

	private final String value;
	private final Set<PathNode> children;

	/**
	 * Creates a leaf node for the given attribute name.
	 *
	 * @param value the JPA attribute name; must not be {@code null}
	 * @throws IllegalArgumentException if {@code value} is {@code null}
	 */
	public PathNode(String value) {
		if (value == null) {
			throw new IllegalArgumentException("value is required");
		}
		this.value = value;
		this.children = new HashSet<PathNode>();
	}

	/**
	 * Returns the JPA attribute name this node represents.
	 *
	 * @return the attribute name, never {@code null}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the mutable set of child nodes.
	 *
	 * @return the children of this node
	 */
	public Set<PathNode> getChildren() {
		return children;
	}

	/**
	 * Merges another node's children into this node, if both nodes represent the
	 * same attribute.
	 *
	 * <p>Children present in {@code that} but absent from this node are added
	 * directly. Children present in both are merged recursively, so that shared
	 * prefixes converge into a single subgraph path.
	 *
	 * @param that the node to merge into this one
	 * @return {@code true} if the merge was performed (i.e. both nodes have equal
	 *         values); {@code false} otherwise
	 */
	boolean merge(PathNode that) {

		if (! this.equals(that)) {
			return false;
		}

		mergeChildren(that.children);
		return true;
	}

	private void mergeChildren(Set<PathNode> childrenToMerge) {
		for (PathNode childToMerge : childrenToMerge) {
			if (children.contains(childToMerge)) {
				getChild(childToMerge.value).mergeChildren(childToMerge.children);
			}
			else {
				children.add(childToMerge);
			}
		}
	}

	/**
	 * Returns the child node with the given attribute name, or {@code null} if no
	 * such child exists.
	 *
	 * @param value the attribute name to look up
	 * @return the matching child, or {@code null}
	 */
	public PathNode getChild(String value) {
		return children.stream()
				.filter(child -> child.getValue().equals(value))
				.findFirst()
				.orElse(null);
	}

	/** Hashes by {@link #value} only. */
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	/** Equals by {@link #value} only. */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathNode other = (PathNode) obj;
		return this.value.equals(other.value);
	}

	/**
	 * Applies this node to the given graph.
	 *
	 * <p>If this node is a leaf, its value is added as an attribute node. If it
	 * has children, a subgraph is created for its value and each child is applied
	 * to that subgraph recursively.
	 *
	 * @param graph the entity graph or subgraph to populate
	 */
	void addToGraph(Graph<?> graph) {
		if (children.isEmpty()) {
			graph.addAttributeNodes(value);
		}
		else {
			Subgraph<?> subgraph = graph.addSubgraph(value);
			children.forEach(child -> child.addToGraph(subgraph));
		}
	}
}
