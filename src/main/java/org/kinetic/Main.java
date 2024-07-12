package org.kinetic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.kinetic.heap.KineticElement;
import org.kinetic.heap.KineticHeap;

public class Main {

  public static void main(String[] args) throws IOException {
    KineticHeap kineticHeap = new KineticHeap();

    List<KineticElement> elements = prepareKineticElements(kineticHeap);
    for (KineticElement element : elements) {
      kineticHeap.insert(element);
    }

    HeapImageCreator imageCreator = new HeapImageCreator(kineticHeap, "example");

    for (int i = 0; i < 20; i++) {
      kineticHeap.fastForward(i);
      imageCreator.process(i);
    }

    kineticHeap.extractMin();
    kineticHeap.insert(new KineticElement(4, 40, 0.4, kineticHeap::getCurTime));
    kineticHeap.fastForward(20);
    imageCreator.process(20);

    kineticHeap.fastForward(34);
    imageCreator.process(34);

  }



  private static List<KineticElement> prepareKineticElements(KineticHeap heap) {
    List<KineticElement> result = Arrays.asList(
        new KineticElement(1, 1.0, 1.8, heap::getCurTime),
        new KineticElement(2, 2.0, 1.6, heap::getCurTime),
        new KineticElement(3, 3.0, 1.5, heap::getCurTime),
        new KineticElement(4, 20, 0.5, heap::getCurTime),
        new KineticElement(5, 4.0, 1.4, heap::getCurTime),
        new KineticElement(6, 5.0, 1.2, heap::getCurTime)
    );

    Collections.shuffle(result);
    return result;

  }

}