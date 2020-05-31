package com.gifola.timer;

public interface CircularTimerListener {
    String updateDataOnTick(long remainingTimeInMs);
    void onTimerFinished();
}
