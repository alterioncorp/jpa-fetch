package io.github.alterioncorp.jpa.fetch;

import java.util.Arrays;

/**
 * Converts {@link FetchPath} instances and dot-separated strings into a {@link PathTree}.
 *
 * <p>Each dot-separated string is split on {@code '.'} and turned into a linked chain of
 * {@link PathNode}s, which is inserted into the tree with prefix-merging so that overlapping
 * paths share a common root.
 */
class PathParser {

	private PathParser() {}

	/**
	 * Builds a {@link PathTree} from {@link FetchPath} instances.
	 *
	 * @param fetchPaths fetch path instances supplying segment arrays
	 * @return the resulting tree
	 */
	static PathTree buildTree(FetchPath... fetchPaths) {
		PathTree tree = new PathTree();
		for (FetchPath fp : fetchPaths) {
			tree.addNode(buildNode(fp.segments()));
		}
		return tree;
	}

	/**
	 * Builds a {@link PathTree} from dot-separated string paths.
	 *
	 * @param paths dot-separated attribute paths (e.g. {@code "organization.country"})
	 * @return the resulting tree
	 */
	static PathTree buildTree(String... paths) {
		PathTree tree = new PathTree();
		Arrays.stream(paths).map(PathParser::buildNode).forEach(tree::addNode);
		return tree;
	}

	/**
	 * Builds the root {@link PathNode} for a dot-separated path string.
	 *
	 * @param path a dot-separated attribute path (e.g. {@code "organization.country"})
	 * @return the root node of the resulting chain
	 */
	static PathNode buildNode(String path) {
		return buildNode(path.split("\\."), 0);
	}

	/**
	 * Builds the root {@link PathNode} for a pre-split segment array.
	 *
	 * @param segments attribute name segments (e.g. {@code ["organization", "country"]})
	 * @return the root node of the resulting chain
	 */
	static PathNode buildNode(String[] segments) {
		if (segments.length == 0) {
			throw new IllegalArgumentException("FetchPath must have at least one segment");
		}
		return buildNode(segments, 0);
	}

	private static PathNode buildNode(String[] segments, int startPosition) {
		PathNode node = new PathNode(segments[startPosition]);
		if (startPosition < (segments.length - 1)) {
			node.getChildren().add(buildNode(segments, startPosition + 1));
		}
		return node;
	}
}
