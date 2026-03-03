package io.github.alterioncorp.jpa.fetch;

import java.util.Arrays;

import com.querydsl.core.types.Path;

/**
 * Converts QueryDSL paths and dot-separated strings into a {@link PathTree}.
 *
 * <p>QueryDSL paths are normalised to dot-separated attribute strings before
 * parsing: {@code any()} calls and parentheses are stripped, and the root
 * variable prefix (e.g. the leading {@code "person"} in
 * {@code "person.organization.country"}) is removed, leaving only the
 * attribute chain relative to the entity (e.g. {@code "organization.country"}).
 *
 * <p>Each dot-separated string is then split on {@code '.'} and turned into a
 * linked chain of {@link PathNode}s, which is inserted into the tree
 * with prefix-merging so that overlapping paths share a common root.
 */
class PathParser {

	private PathParser() {}

	/**
	 * Builds a {@link PathTree} from QueryDSL path references.
	 *
	 * @param paths Q-type path references
	 *              (e.g. {@code QPerson.person.organization().country()})
	 * @return the resulting tree
	 */
	public static PathTree buildTree(Path<?>... paths) {

		String[] pathsAsStrings = Arrays.stream(paths)
			.map(PathParser::pathToString)
			.toArray(String[]::new);

		return buildTree(pathsAsStrings);
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

	private static PathNode buildNode(String[] pathParsed, int startPosition) {
		PathNode node = new PathNode(pathParsed[startPosition]);
		if (startPosition < (pathParsed.length - 1)) {
			node.getChildren().add(buildNode(pathParsed, startPosition + 1));
		}
		return node;
	}

	/**
	 * Converts a QueryDSL {@link Path} to a dot-separated attribute string
	 * relative to the entity root.
	 *
	 * <p>For example, {@code QPerson.person.organization().country()} becomes
	 * {@code "organization.country"}, and
	 * {@code QOrganization.organization.persons.any().role()} becomes
	 * {@code "persons.role"}.
	 *
	 * @param dslQueryPath the QueryDSL path to convert
	 * @return the normalised dot-separated attribute string
	 */
	static String pathToString(Path<?> dslQueryPath) {
		String path = dslQueryPath.toString();
		path = path.replaceAll("any\\(", "");
		path = path.replaceAll("\\)", "");
		path = path.substring(path.indexOf('.') + 1);
		return path;
	}
}
