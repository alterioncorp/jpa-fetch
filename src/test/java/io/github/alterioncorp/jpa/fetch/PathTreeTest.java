package io.github.alterioncorp.jpa.fetch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.Subgraph;

class PathTreeTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testAddToGraph() {

		EntityGraph entityGraph = Mockito.mock(EntityGraph.class);
		PathTree pathTree = new PathTree();
		pathTree.getRoots().add(new PathNode("a"));
		pathTree.addToGraph(entityGraph);
		Mockito.verify(entityGraph, Mockito.times(1)).addAttributeNodes("a");

		entityGraph = Mockito.mock(EntityGraph.class);
		Subgraph subgraph = Mockito.mock(Subgraph.class);
		Mockito.when(entityGraph.addSubgraph(Mockito.anyString())).thenReturn(subgraph);
		pathTree = new PathTree();
		PathNode pathNode = PathParser.buildNode("a.b");
		pathTree.getRoots().add(pathNode);
		pathTree.addToGraph(entityGraph);
		Mockito.verify(entityGraph, Mockito.times(1)).addSubgraph("a");
		Mockito.verify(subgraph, Mockito.times(1)).addAttributeNodes("b");
	}

	@Test
	public void testAddNode() {

		PathTree pathTree = new PathTree();
		pathTree.addNode(new PathNode("a"));
		Assertions.assertEquals(1, pathTree.getRoots().size());
		Assertions.assertNotNull(pathTree.getRoot("a"));

		pathTree.addNode(new PathNode("b"));
		Assertions.assertEquals(2, pathTree.getRoots().size());
		Assertions.assertNotNull(pathTree.getRoot("b"));

		pathTree.addNode(new PathNode("b"));
		Assertions.assertEquals(2, pathTree.getRoots().size());
		Assertions.assertNotNull(pathTree.getRoot("b"));

		PathNode pathNode = PathParser.buildNode("b.c");
		pathTree.addNode(pathNode);
		Assertions.assertEquals(2, pathTree.getRoots().size());
		Assertions.assertNotNull(pathTree.getRoot("b").getChild("c"));
	}

	@Test
	public void testGetRoot() {

		PathTree pathTree = new PathTree();
		pathTree.addNode(new PathNode("a"));

		Assertions.assertNull(pathTree.getRoot("b"));
		Assertions.assertNotNull(pathTree.getRoot("a"));
		Assertions.assertEquals("a", pathTree.getRoot("a").getValue());
	}
}
