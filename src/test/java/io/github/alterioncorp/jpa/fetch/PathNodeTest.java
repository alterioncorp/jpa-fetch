package io.github.alterioncorp.jpa.fetch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.Subgraph;


class PathNodeTest {

	@Test
	public void testConstructor() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new PathNode(null));
	}

	@Test
	public void testMerge() {

		PathNode pathNode1 = PathParser.buildNode("a.b");
		PathNode pathNode2 = PathParser.buildNode("c.d");
		Assertions.assertFalse(pathNode1.merge(pathNode2));

		pathNode1 = PathParser.buildNode("a.b");
		pathNode2 = PathParser.buildNode("a.c");
		Assertions.assertTrue(pathNode1.merge(pathNode2));
		Assertions.assertEquals(2, pathNode1.getChildren().size());
		Assertions.assertTrue(pathNode1.getChildren().contains(new PathNode("b")));
		Assertions.assertTrue(pathNode1.getChildren().contains(new PathNode("c")));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testAddToGraph() {

		EntityGraph entityGraph = Mockito.mock(EntityGraph.class);
		PathNode pathNode = PathParser.buildNode("a");
		pathNode.addToGraph(entityGraph);
		Mockito.verify(entityGraph, Mockito.times(1)).addAttributeNodes("a");

		entityGraph = Mockito.mock(EntityGraph.class);
		Subgraph subgraph = Mockito.mock(Subgraph.class);
		Mockito.when(entityGraph.addSubgraph(Mockito.anyString())).thenReturn(subgraph);
		pathNode = PathParser.buildNode("a.b");
		pathNode.addToGraph(entityGraph);
		Mockito.verify(entityGraph, Mockito.times(1)).addSubgraph("a");
		Mockito.verify(subgraph, Mockito.times(1)).addAttributeNodes("b");
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals() {
		PathNode pathNode = new PathNode("a");
		Assertions.assertFalse(pathNode.equals(null));
		Assertions.assertTrue(pathNode.equals(pathNode));
		Assertions.assertFalse(pathNode.equals("b"));
		Assertions.assertFalse(pathNode.equals(new PathNode("b")));
		Assertions.assertTrue(pathNode.equals(new PathNode("a")));
	}

	@Test
	public void testHashCode() {
		PathNode node1 = new PathNode("a");
		PathNode node2 = new PathNode("a");
		PathNode node3 = new PathNode("b");
		Assertions.assertEquals(node1.hashCode(), node1.hashCode());
		Assertions.assertEquals(node1.hashCode(), node2.hashCode());
		Assertions.assertNotEquals(node1.hashCode(), node3.hashCode());
	}

	@Test
	public void testGetNode() {
		PathNode pathNode = PathParser.buildNode("a.b");
		Assertions.assertNull(pathNode.getChild("c"));
		Assertions.assertNotNull(pathNode.getChild("b"));
		Assertions.assertEquals("b", pathNode.getChild("b").getValue());
	}
}
