package io.github.alterioncorp.jpa.fetch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.alterioncorp.jpa.fetch.entities.path.QOrganization;
import io.github.alterioncorp.jpa.fetch.entities.path.QPerson;

class PathParserTest {

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

	@Test
	public void testPathToString() {
		Assertions.assertEquals("organization", PathParser.pathToString(QPerson.person.organization()));
		Assertions.assertEquals("organization.persons", PathParser.pathToString(QPerson.person.organization().persons));
		Assertions.assertEquals("persons", PathParser.pathToString(QOrganization.organization.persons));
	}
}
