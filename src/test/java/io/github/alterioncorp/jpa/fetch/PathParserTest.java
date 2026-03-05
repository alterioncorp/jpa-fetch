package io.github.alterioncorp.jpa.fetch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PathParserTest {

	@Test
	public void testBuildNode_emptySegments_throws() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> PathParser.buildNode(new String[0]));
	}

	@Test
	public void testBuildFromPaths() {

		PathTree tree = PathParser.buildTree("11.21.31", "12.22", "11.21.32");
		Assertions.assertEquals(2, tree.getRoots().size());
		Assertions.assertNotNull(tree.getRoot("11"));
		Assertions.assertNotNull(tree.getRoot("12"));
		Assertions.assertNotNull(tree.getRoot("11").getChild("21"));
		Assertions.assertNotNull(tree.getRoot("11").getChild("21").getChild("31"));
		Assertions.assertNotNull(tree.getRoot("11").getChild("21").getChild("32"));
		Assertions.assertNotNull(tree.getRoot("12").getChild("22"));
	}
}
