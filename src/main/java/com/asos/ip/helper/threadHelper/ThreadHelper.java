package com.asos.ip.helper.threadHelper;

public class ThreadHelper {

    private long threadProcessStartTime;
    private long threadProcessEndTime;


    public ThreadHelper() {}

    public void waitToAllowFlowProcessing(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    public void startThreadProcessTimer() {
        threadProcessStartTime = System.currentTimeMillis();  // Capture the current time at the start
    }

    public void stopThreadProcessTimer() {
        threadProcessEndTime = System.currentTimeMillis();  // Capture the current time at the stop
    }

    public double getElapsedThreadProcessTimeInSeconds() {
        return (threadProcessEndTime - threadProcessStartTime) / 1000.0;  // Calculate the elapsed time in seconds
    }

}
