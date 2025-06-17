package com.asos.ip.steps;

import com.asos.ip.helper.threadHelper.ThreadHelper;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;

public class ThreadSteps {

    private final ThreadHelper threadHelper;

    @Autowired
    public ThreadSteps(ThreadHelper threadHelper) {
        this.threadHelper = threadHelper;
    }

    @And("I wait for flow to process for {long} milliseconds")
    public void waitForFlowToProcessMilliseconds(long milis) throws InterruptedException {
        threadHelper.waitToAllowFlowProcessing(milis);
    }

    @And("I wait for flow to process for {long} seconds")
    public void waitForFlowToProcessSeconds(long seconds) throws InterruptedException {
        threadHelper.waitToAllowFlowProcessing(seconds * 1000);
    }

}
