package com.nikoskatsanos.benchmarks.singlevsmanythreads;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingQueueBackedActor implements SetBitsCountStrategy {

    private BlockingQueue<Long> queue = new ArrayBlockingQueue<>(32, true);

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

    public BlockingQueueBackedActor() {
        this.start();
    }

    private void start() {
        executors.execute(() -> {
            while (true) {
                try {
                    final Long value = this.queue.take();
                    final short setBits = SetBitsCalculator.countSetBits(value);

                    if (setBits % 2 == 0) {
                        this.evenCount.incrementAndGet();
                    } else {
                        this.oddCount.incrementAndGet();
                    }
                } catch (final InterruptedException e) {
                }
            }
        });
    }

    @Override
    public void calc(long number) {
        try {
            this.queue.put(number);
        } catch (final InterruptedException e) {
        }
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
