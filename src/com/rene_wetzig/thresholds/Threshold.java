package com.rene_wetzig.thresholds;

public abstract class Threshold {

    private final int windowSize;
    private int currentThreshold;
    private int counter;

    // initialise this threshold.
    public Threshold(int windowSize){
        this.windowSize = windowSize;
        counter = 0;
    };

    // inserts new Score into Threshold and returns false if Sample is recognised as an anomaly, true if it's recognised as a normal sample
    public abstract boolean insertNewSample(int anomalyScore);

    // returns false if Sample is recognised as an anomaly, true if it's recognised as a normal sample.
    public boolean predictSample(int anomalyScore){
        if(!referenceCreated()) return true;
        return anomalyScore > currentThreshold;
    }

    public abstract String toString();

    public int getCurrentThreshold(){
        return currentThreshold;
    }

    public void setCurrentThreshold(int newThreshold) {
        this.currentThreshold = newThreshold;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public boolean referenceCreated(){
        if(counter < windowSize){
            counter++;
            return false;
        } else {
            return true;
        }
    }
}
