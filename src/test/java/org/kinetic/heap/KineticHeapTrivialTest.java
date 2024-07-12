package org.kinetic.heap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kinetic.Utils;

class KineticHeapTrivialTest {

  private KineticHeapTrivial kineticHeap;


  @BeforeEach
  public void setUp() {
    kineticHeap = new KineticHeapTrivial();

  }

  @Test
  public void testValidCertificatesAfterAdding() {
    IntStream.range(1, 100).forEach(id -> {
      KineticElement kineticElement = new KineticElement(id,
          ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime());
      kineticHeap.insert(kineticElement);
      assertThat(heapChecker(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          0, kineticHeap.size() - 1)).isTrue();
    });
  }

  @Test
  public void testValidCertificatesAfterRemove() {
    IntStream.range(1, 100).forEach(id -> {
      KineticElement kineticElement = new KineticElement(id,
          ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime());
      kineticHeap.insert(kineticElement);
    });

    while (kineticHeap.size() > 0) {
      kineticHeap.extractMin();
      assertThat(heapChecker(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          0, kineticHeap.size() - 1)).isTrue();
    }
  }

  @Test
  public void testValidCertificatesRemoveInserts() {
    IntStream.range(1, 100).forEach(id -> {
      kineticHeap.insert(new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime()));
      kineticHeap.insert(new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime()));
      assertThat(heapChecker(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          0, kineticHeap.size() - 1)).isTrue();

      kineticHeap.extractMin();
      assertThat(heapChecker(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          0, kineticHeap.size() - 1)).isTrue();
    });

    while (kineticHeap.size() > 0) {
      kineticHeap.extractMin();
      assertThat(heapChecker(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          0, kineticHeap.size() - 1)).isTrue();
    }
  }

  @Test
  public void testValidCertificatesAfterAddingMoveTime()
      throws IOException, CsvValidationException {
    List<KineticElement> list = new ArrayList<>();
    String file = KineticHeapTest.class.getClassLoader().getResource("scenario1.csv").getFile();

    try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(file)).withSkipLines(1)
        .build()) {
      String[] values;
      while ((values = csvReader.readNext()) != null) {
        list.add(new KineticElement(Integer.parseInt(values[0]), Double.parseDouble(values[2]),
            Double.parseDouble(values[1]), () -> kineticHeap.getCurTime()));
      }
    }

    list.forEach(e -> {
      KineticElement kineticElement = new KineticElement(e.getId(),
          e.getInitialPriority(),
          e.getRate(), () -> kineticHeap.getCurTime());
      kineticHeap.insert(kineticElement);
      assertThat(heapChecker(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          0, kineticHeap.size() - 1)).isTrue();
    });

    List<KineticElement[]> pairs = Utils.kineticElementsPermutations(kineticHeap.getHeap().getHeapList());
    double maxIntersectionTime = pairs.stream().map(p -> p[0].getIntersectionTime(p[1]))
        .filter(p -> p >= 0).mapToDouble(x -> x).max().orElse(-1.0);

    int t = 0;
    while (true) {
      kineticHeap.fastForward(t);
      assertThat(heapChecker(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          0, kineticHeap.size() - 1)).isTrue();

      // just to make one my cycle
      if (t - 1 > maxIntersectionTime) {
        break;
      }

      t++;
    }

  }


  private boolean heapChecker(KineticElement[] elements, int i, int n) {
    if (i >= (n - 1) / 2) {
      return true;
    }

    KineticElement thisElement = elements[i];
    KineticElement leftChild = elements[Heap.getLeftChild(i)];
    KineticElement rightChild = elements[Heap.getRightChild(i)];

    if (thisElement.getPriority() > leftChild.getPriority()
        || thisElement.getPriority() > rightChild.getPriority()) {
      return false;
    }

    return heapChecker(elements, Heap.getLeftChild(i), n)
        && heapChecker(elements, Heap.getRightChild(i), n);

  }

}