package org.kinetic;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import lombok.RequiredArgsConstructor;
import org.kinetic.heap.Certificate;
import org.kinetic.heap.Heap;
import org.kinetic.heap.KineticElement;
import org.kinetic.heap.KineticHeap;
import com.google.common.html.HtmlEscapers;


@RequiredArgsConstructor
public class HeapImageCreator {

  private final static DecimalFormat FORMATTER = new DecimalFormat("#0.00");

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
    Graphviz.fromGraph(graph).width(2000).height(1500).render(Format.PNG).toFile(file);
  }

  private Node createNode(KineticHeap kineticHeap, int index) {
    if (index >= kineticHeap.getHeap().size()) {
      return null;
    }

    KineticElement kineticElement = kineticHeap.getHeap().getHeapList().get(index);

    String textId = String.valueOf(kineticHeap.getHeap().getHeapList().get(index));
    return node(textId).with(Label.html("<b>" + kineticElement.getId() + " ( RATE: " +FORMATTER.format(kineticElement.getRate()) + ")</b><br/>" +
        "[P0: " + FORMATTER.format(kineticElement.getInitialPriority()) + "]<br/>" +
        "[P" + kineticHeap.getCurTime() + ": " + FORMATTER.format(kineticElement.getPriority()) + "]<br/>" +
        certToString(kineticHeap.getHeap(), kineticElement.getCertificate())
      ));
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

  private static String certToString(Heap<KineticElement> heap, Certificate certificate) {
    if (certificate == null || certificate.getElementIdx() == Heap.getRoot()) {
      return "N/A";
    }

    return "[" + heap.getValue(Heap.getParent(certificate.getElementIdx())).getId() + HtmlEscapers.htmlEscaper().escape("]<[") + heap.getValue(certificate.getElementIdx()).getId()
        + "] I: "
        + FORMATTER.format(certificate.getExpirationTime());
  }

}
