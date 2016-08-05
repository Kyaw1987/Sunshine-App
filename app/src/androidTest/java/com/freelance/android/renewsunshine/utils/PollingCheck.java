package com.freelance.android.renewsunshine.utils;

import junit.framework.Assert;

/**
 * Created by Administrator on 07/26/2016.
 */
public abstract class PollingCheck {

    private static final long TIME_SLICE = 50;
    private long mTimeout = 3000;

    public PollingCheck(long mTimeout) {
        this.mTimeout = mTimeout;
    }

    protected abstract boolean check();

    public void run() {
        if (check()) {
            return;
        }

        long timeout = mTimeout;
        while (timeout > 0) {
            try {
                Thread.sleep(TIME_SLICE);
            } catch (InterruptedException e) {
                Assert.fail("unexpected InterruptedException");
            }

            if (check()) {
                return;
            }

            timeout -= TIME_SLICE;
        }

        Assert.fail("unexpected timeout");
    }
}
