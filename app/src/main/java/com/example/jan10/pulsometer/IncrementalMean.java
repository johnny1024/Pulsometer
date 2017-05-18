package com.example.jan10.pulsometer;

public class IncrementalMean {

    private double sumOfValues = 0;
    private double noOfValues = 0;
    private double lastValue = 0;

    private final Object mutex = new Object();

    void addValue(double value) {
        synchronized (mutex) {
            sumOfValues += value;
            noOfValues += 1;
            lastValue = value;
        }
    }

    double getMean() {
        synchronized (mutex) {
            if (sumOfValues == 0) {
                return lastValue;
            }
            return sumOfValues / noOfValues;
        }
    }

    void clear() {
        synchronized (mutex) {
            sumOfValues = 0;
            noOfValues = 0;
        }
    }
}
