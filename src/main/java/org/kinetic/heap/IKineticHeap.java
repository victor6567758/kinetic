package org.kinetic.heap;

public interface IKineticHeap extends IHeap<KineticElement> {

  void fastForward(int nextTime);

  int getCurTime();
}
