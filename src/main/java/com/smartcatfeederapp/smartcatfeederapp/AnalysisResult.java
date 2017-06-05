package com.smartcatfeederapp.smartcatfeederapp;

/**
 * Created by Tom Wong on 6/4/2017.
 */

public class AnalysisResult {
    private int month;
    private double avgConsumption;
    private double netCatWeight;

    public AnalysisResult(int month, double avgConsumption, double netCatWeight) {
        this.month = month;
        this.avgConsumption = avgConsumption;
        this.netCatWeight = netCatWeight;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public double getAvgConsumption() {
        return avgConsumption;
    }

    public void setAvgConsumption(double avgConsumption) {
        this.avgConsumption = avgConsumption;
    }

    public double getNetCatWeight() {
        return netCatWeight;
    }

    public void setNetCatWeight(double netCatWeight) {
        this.netCatWeight = netCatWeight;
    }
}
