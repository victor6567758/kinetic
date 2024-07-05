package org.kinetic;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.kinetic.heap.Heap;
import org.kinetic.heap.KineticElement;
import org.kinetic.heap.KineticHeap;


@RequiredArgsConstructor
public class HeapImageCreator {


  private final KineticHeap kineticHeap;

  private final String targetDir;

  public void process(int time) throws IOException {
    createImage(kineticHeap, time);
  }

  private void createImage(KineticHeap kineticHeap, int time) throws IOException {
    Node root = createNode(kineticHeap, 0);
    root = buildTree(kineticHeap, root, 0);

    File file = new File(this.targetDir, "t" + time + "_moment.png");
    Graph graph = graph("Graph: " + time).directed().with(root);
    Graphviz.fromGraph(graph).width(2500).height(1000).render(Format.PNG).toFile(file);
  }

  private Node createNode(KineticHeap kineticHeap, int index) {
    if (index >= kineticHeap.getHeap().size()) {
      return null;
    }
    String textId = String.valueOf(kineticHeap.getHeap().getHeapList().get(index));
    return node(textId);
  }

  public Node buildTree(KineticHeap kineticHeap, Node root, int index) {
    if (root == null) {
      return null;
    }
    if (index >= kineticHeap.getHeap().size()) {
      return null;
    }

    Node result = root;
    int leftIdx = Heap.getLeftChild(index);
    Node newLeftNode = buildTree(kineticHeap, createNode(kineticHeap, leftIdx), leftIdx);
    if (newLeftNode != null) {
      result = result.link(newLeftNode);
    }

    int rightIdx = Heap.getRightChild(index);
    Node newRightNode = buildTree(kineticHeap, createNode(kineticHeap, rightIdx), rightIdx);
    if (newRightNode != null) {
      result = result.link(newRightNode);
    }

    return result;
  }

}
