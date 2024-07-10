package org.kinetic.heap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kinetic.HeapImageCreator;
import org.kinetic.Utils;


class KineticHeapTest {

  private KineticHeap kineticHeap;


  @BeforeEach
  public void setUp() {
    kineticHeap = new KineticHeap();

     }

  @Test
  public void testValidCertificatesAfterAdding() {
    IntStream.range(1, 100).forEach(id -> {
      KineticElement kineticElement = new KineticElement(id,
          ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime());
      kineticHeap.insert(kineticElement);
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
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
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
    }
  }

  @Test
  public void testValidCertificatesRemoveInserts() {
    IntStream.range(1, 100).forEach(id -> {
      kineticHeap.insert(new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime()));
      assertCertificatesMatchElements();
      kineticHeap.insert(new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime()));
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
      assertCertificatesMatchElements();

      kineticHeap.extractMin();
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
      assertCertificatesMatchElements();
    });

    while (kineticHeap.size() > 0) {
      kineticHeap.extractMin();
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
      assertCertificatesMatchElements();
    }
  }

  @Test
  public void testScenario2MoveTime() throws IOException, CsvValidationException {

    HeapImageCreator heapImageCreator = new HeapImageCreator(kineticHeap, "testExample/testBulkMoveTime");

    List<KineticElement>  list = readResourceData("scenario2.csv");

    list.forEach(e -> {

      KineticElement kineticElement = new KineticElement(e.getId(),
          e.getInitialPriority(),
          e.getRate(), () -> kineticHeap.getCurTime());
      kineticHeap.insert(kineticElement);
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
      assertCertificatesFutureTimeInQueue(0);
      assertCertificatesMatchElements();
    });

    List<KineticElement[]> pairs = Utils.kineticElementsPermutations(kineticHeap.getHeap().getHeapList());
    double maxIntersectionTime = pairs.stream().map(p -> p[0].getIntersectionTime(p[1]))
        .filter(p -> p >= 0).mapToDouble(x -> x).max().orElse(-1.0);

    int t = 0;
    while (t <= maxIntersectionTime) {
      kineticHeap.fastForward(t);
      heapImageCreator.process(t);
      assertCertificatesMatchElements();
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).withFailMessage("Time: " + t).isTrue();
      assertCertificatesFutureTimeInQueue(t);

      t += 1;
    }

  }


  @Test
  public void testScenario1MoveTime()
      throws IOException, CsvValidationException {
    HeapImageCreator heapImageCreator = new HeapImageCreator(kineticHeap, "testExample/testValidCertificatesAfterAddingMoveTime");

    List<KineticElement> list = readResourceData("scenario1.csv");

    list.forEach(e -> {

      KineticElement kineticElement = new KineticElement(e.getId(),
          e.getInitialPriority(),
          e.getRate(), () -> kineticHeap.getCurTime());
      kineticHeap.insert(kineticElement);
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
      assertCertificatesFutureTimeInQueue(0);
      assertCertificatesMatchElements();
    });

    int t = 0;
    while (true) {
      double maxCertTime = kineticHeap.getHeap().getHeapList().stream()
          .map(KineticElement::getCertificate).
          filter(Objects::nonNull).map(Certificate::getExpirationTime).mapToDouble(n -> n).max()
          .orElse(-1.0);

      kineticHeap.fastForward(t);
      heapImageCreator.process(t);
      assertCertificatesMatchElements();
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
      assertCertificatesFutureTimeInQueue(t);

      // just to make one my cycle
      if (t - 1 > maxCertTime) {
        break;
      }

      t++;
    }

  }

  @Test
  public void kineticHeapAddTimeForwardBigSteps() {
    List<KineticElement> kineticElements = new ArrayList<>();

    for (int id = 1; id <= 100; id++) {
      KineticElement kineticElement = new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime());

      kineticElements.add(kineticElement);
    }

    List<KineticElement[]> pairs = Utils.kineticElementsPermutations(kineticElements);
    double lastTime = (int)pairs.stream().map(p -> p[0].getIntersectionTime(p[1]))
        .filter(p -> p >= 0).mapToDouble(x -> x).max().orElse(-1.0);
    double timeStepDuration = lastTime / 100;
    if (timeStepDuration == 0) {
      timeStepDuration = 1;
    }

    kineticElements.forEach(e -> {
      kineticHeap.insert(e);
    });

    int t = 0;
    while (t <=lastTime) {
      kineticHeap.fastForward(t);
      t += timeStepDuration;
    }
  }


  private boolean checkElementsCorrect(KineticElement[] elements, int curTime, int i, int n) {
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

    if (leftChild.getCertificate() != null) {
      if (leftChild.getCertificate().getOwnIdx() == -1
          || leftChild.getCertificate().getExpirationTime() < curTime) {
        return false;
      }
    }

    if (rightChild.getCertificate() != null) {
      if (rightChild.getCertificate().getOwnIdx() == -1
          || rightChild.getCertificate().getExpirationTime() < curTime) {
        return false;
      }
    }

    return checkElementsCorrect(elements, curTime, Heap.getLeftChild(i), n)
        && checkElementsCorrect(elements, curTime, Heap.getRightChild(i), n);

  }


  private void assertCertificatesMatchElements() {
    Certificate[] certHeapArray =  kineticHeap.getCertificates().getHeapArray(Certificate.class);

    for (int i = 0; i < certHeapArray.length; i++) {
      assertThat(certHeapArray[i].getOwnIdx()).isGreaterThanOrEqualTo(0);
      assertThat(certHeapArray[i].getOwnIdx()).isEqualTo(i);
    }

    List<KineticElement> elements =  kineticHeap.getHeap().getHeapList();
    for (int i = 0; i < elements.size(); i++) {
      Certificate certificate = elements.get(i).getCertificate();
      if (certificate != null) {
        assertThat(certHeapArray[certificate.getOwnIdx()].getElementIdx()).isEqualTo(i);
      }
    }

  }

  private void assertCertificatesFutureTimeInQueue(int t) {
    List<KineticElement> elements =  kineticHeap.getHeap().getHeapList();
    List<Certificate> certificates = kineticHeap.getCertificates().getHeapList();

    assertThat(certificates.stream().filter(e -> e.getExpirationTime() >= t).count()).isEqualTo(elements.stream().filter( e-> e.getCertificate() != null).count());

    for (int i = 0; i < elements.size(); i++) {
      int idx = i;
      KineticElement element = elements.get(i);
      if (element.getCertificate() != null && element.getCertificate().getExpirationTime() >= t) {
        assertThat(certificates.get(element.getCertificate().getOwnIdx()).getElementIdx()).isEqualTo(idx);
        assertThat(kineticHeap.getCertificates().getHeapList().stream().filter( c -> c.getElementIdx() == idx).count()).isGreaterThan(0);
      }
    }
  }

  private List<KineticElement> readResourceData(String fileName)
      throws IOException, CsvValidationException {
    List<KineticElement> list = new ArrayList<>();
    String file = KineticHeapTest.class.getClassLoader().getResource(fileName).getFile();

    try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(file)).withSkipLines(1)
        .build()) {
      String[] values;
      while ((values = csvReader.readNext()) != null) {
        if (values[0].length() == 0) {
          break;
        }
         list.add(new KineticElement(Integer.parseInt(values[0]), Double.parseDouble(values[2]),
            Double.parseDouble(values[1]), () -> kineticHeap.getCurTime()));
      }
    }

    return list;
  }

}