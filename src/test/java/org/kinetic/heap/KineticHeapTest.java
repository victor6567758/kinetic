package org.kinetic.heap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kinetic.HeapImageCreator;
import org.kinetic.Utils;


class KineticHeapTest {

  private static final int BULK_INSERT_NUM = 1_000_000;
  private static final int BIG_STEPS_NUM = 10_000;
  private static final int BIG_STEPS_STEPS = 1000;

  private KineticHeap kineticHeap;


  @BeforeEach
  public void setUp() {
    kineticHeap = new KineticHeap();
  }

  @Test
  public void testSingleIntersectionTwoElements() {
    KineticElement e1 = new KineticElement(1, 3.0, 4.0, () -> kineticHeap.getCurTime());
    KineticElement e2 = new KineticElement(2, 0.0, 0.8, () -> kineticHeap.getCurTime());
    KineticElement e3 = new KineticElement(3, 1.0, 0.5, () -> kineticHeap.getCurTime());

    kineticHeap.insert(e1);
    kineticHeap.insert(e2);
    kineticHeap.insert(e3);

    assertCertificatesMatchElements(kineticHeap);
    assertElementsCorrect(kineticHeap);

    kineticHeap.fastForward(4);
    assertCertificatesMatchElements(kineticHeap);
    assertElementsCorrect(kineticHeap);

  }

  @Test
  public void testReverseOrderElements() {
    KineticElement e1 = new KineticElement(1, 1.0, 0.5, () -> kineticHeap.getCurTime());
    KineticElement e2 = new KineticElement(2, 0.0, 0.8, () -> kineticHeap.getCurTime());
    KineticElement e3 = new KineticElement(3, 3.0, 0.07, () -> kineticHeap.getCurTime());

    kineticHeap.insert(e1);
    kineticHeap.insert(e2);
    kineticHeap.insert(e3);

    kineticHeap.fastForward(3);
    assertCertificatesMatchElements(kineticHeap);
    assertElementsCorrect(kineticHeap);

    kineticHeap.fastForward(4);
    assertCertificatesMatchElements(kineticHeap);
    assertElementsCorrect(kineticHeap);

    kineticHeap.fastForward(5);
    assertCertificatesMatchElements(kineticHeap);
    assertElementsCorrect(kineticHeap);

  }

  @Test
  public void testValidCertificatesAfterAdding() {
    IntStream.range(1, 100).forEach(id -> {
      KineticElement kineticElement = new KineticElement(id,
          ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime());
      kineticHeap.insert(kineticElement);
      assertElementsCorrect(kineticHeap);
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
      assertElementsCorrect(kineticHeap);
    }
  }

  @Test
  public void testValidCertificatesRemoveInserts() {
    IntStream.range(1, 100).forEach(id -> {
      kineticHeap.insert(new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime()));
      assertCertificatesMatchElements(kineticHeap);
      kineticHeap.insert(new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime()));
      assertThat(checkElementsCorrect(kineticHeap.getHeap().getHeapArray(KineticElement.class),
          kineticHeap.getCurTime(), 0, kineticHeap.size() - 1)).isTrue();
      assertCertificatesMatchElements(kineticHeap);

      kineticHeap.extractMin();
      assertElementsCorrect(kineticHeap);
      assertCertificatesMatchElements(kineticHeap);
    });

    while (kineticHeap.size() > 0) {
      kineticHeap.extractMin();
      assertElementsCorrect(kineticHeap);
      assertCertificatesMatchElements(kineticHeap);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "scenario1.csv",
      "scenario2.csv",
      "scenario3.csv",
      "scenario4.csv"
  })
  public void testScenariosMoveTime(String file) throws IOException, CsvValidationException {

    String dirName = FilenameUtils.getBaseName(file);
    HeapImageCreator heapImageCreator = new HeapImageCreator(kineticHeap,
        new File("testExample", dirName).getPath());
    List<KineticElement> list = readResourceData(file);

    list.forEach(e -> {

      KineticElement kineticElement = new KineticElement(e.getId(),
          e.getInitialPriority(),
          e.getRate(), () -> kineticHeap.getCurTime());
      kineticHeap.insert(kineticElement);

      assertElementsCorrect(kineticHeap);
      assertCertificatesFutureTimeInQueue(0);
      assertCertificatesMatchElements(kineticHeap);
    });


    double maxIntersectionTime = Utils.maxTimeForPermutations(kineticHeap);

    int t = 0;
    while (t + 1 <= maxIntersectionTime) {
      kineticHeap.fastForward(t);
      heapImageCreator.process(t);

      assertCertificatesMatchElements(kineticHeap);
      assertElementsCorrect(kineticHeap);
      assertCertificatesFutureTimeInQueue(t);

      t += 1;
    }

  }


  @Test
  public void kineticHeapAddTimeForwardBigSteps() {
    List<KineticElement> kineticElements = new ArrayList<>();

    for (int id = 1; id <= BIG_STEPS_NUM; id++) {
      KineticElement kineticElement = new KineticElement(id,
          ThreadLocalRandom.current().nextDouble(0.0, 10.0),
          ThreadLocalRandom.current().nextDouble(0.5, 2.0), () -> kineticHeap.getCurTime());

      kineticElements.add(kineticElement);
    }

    double lastTime = Utils.maxTimeForPermutations(kineticElements);
    int timeStepDuration = (int) lastTime / BIG_STEPS_STEPS;
    if (timeStepDuration == 0) {
      timeStepDuration = 1;
    }

    kineticElements.forEach(e -> {
      kineticHeap.insert(e);
    });

    int t = 0;
    while (t + 1 <= lastTime) {
      kineticHeap.fastForward(t);
      assertElementsCorrect(kineticHeap);

      t += timeStepDuration;
    }
  }

  @Test
  public void testBulkInserts() {
    List<KineticElement> kineticElements = new ArrayList<>();

    for (int id = 1; id <= BULK_INSERT_NUM; id++) {
      kineticElements.add(
          new KineticElement(id, ThreadLocalRandom.current().nextDouble(0.0, 10.0),
              ThreadLocalRandom.current().nextDouble(0.5, 2.0),
              () -> kineticHeap.getCurTime()));
    }

    for (KineticElement element : kineticElements) {
      kineticHeap.insert(element);

    }
    assertElementsCorrect(kineticHeap);
  }

  private void assertElementsCorrect(KineticHeap heap) {
    assertThat(checkElementsCorrect(heap.getHeap().getHeapArray(KineticElement.class),
        heap.getCurTime(), 0, heap.size() - 1)).isTrue();
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


  private void assertCertificatesMatchElements(KineticHeap heap) {
    Certificate[] certHeapArray = heap.getCertificates().getHeapArray(Certificate.class);

    for (int i = 0; i < heap.getCertificates().size(); i++) {
      assertThat(heap.getCertificates().getValue(i).getOwnIdx()).isEqualTo(i);
    }

    for (int i = 0; i < heap.getHeap().size(); i++) {
      Certificate certificate = heap.getHeap().getValue(i).getCertificate();
      if (certificate != null) {
        assertThat(certHeapArray[certificate.getOwnIdx()].getElementIdx()).isEqualTo(i);
      }
    }

  }

  private void assertCertificatesFutureTimeInQueue(int t) {
    List<KineticElement> elements = kineticHeap.getHeap().getHeapList();
    List<Certificate> certificates = kineticHeap.getCertificates().getHeapList();

    assertThat(certificates.stream().filter(e -> e.getExpirationTime() >= t).count()).isEqualTo(
        elements.stream().filter(e -> e.getCertificate() != null).count());

    for (int i = 0; i < kineticHeap.getHeap().size(); i++) {
      int idx = i;
      KineticElement element = kineticHeap.getValue(i);
      if (element.getCertificate() != null && element.getCertificate().getExpirationTime() >= t) {
        assertThat(
            kineticHeap.getCertificates().getValue(element.getCertificate().getOwnIdx()).getElementIdx()).isEqualTo(idx);
        assertThat(kineticHeap.getCertificates().getHeapList().stream()
            .filter(c -> c.getElementIdx() == idx).count()).isGreaterThan(0);
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
        if (values[0].isEmpty()) {
          break;
        }
        list.add(new KineticElement(Integer.parseInt(values[0]), Double.parseDouble(values[2]),
            Double.parseDouble(values[1]), () -> kineticHeap.getCurTime()));
      }
    }

    return list;
  }

}