package org.kinetic;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.kinetic.heap.KineticElement;

@UtilityClass
public class Utils {

  public static List<KineticElement[]> kineticElementsPermutations(List<KineticElement> elements) {
    List<KineticElement[]> permutations = new ArrayList<>();
    int size = elements.size();

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (i != j) {
          permutations.add(new KineticElement[]{elements.get(i), elements.get(j)});
        }
      }
    }

    return permutations;
  }

}
