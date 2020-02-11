package com.nikoskatsanos.benchmarks.singlevsmanythreads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadedActor implements SetBitsCountStrategy {

    private ExecutorService executors = Executors.newFixedThreadPool(4, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    private final AtomicInteger evenCount = new AtomicInteger();
    private final AtomicInteger oddCount = new AtomicInteger();

    @Override
    public void calc(final long number) {

        this.executors.execute(() -> {
            final short setBits = SetBitsCalculator.countSetBits(number);

            if (setBits % 2 == 0) {
                this.evenCount.incrementAndGet();
            } else {
                this.oddCount.incrementAndGet();
            }
        });
    }

    @Override
    public int getEvenCount() {
        return this.evenCount.get();
    }

    @Override
    public int getOddCount() {
        return this.oddCount.get();
    }
}
