package com.nikoskatsanos.benchmarks.gc;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * A very naive class that puts in test a few GCs, with the goal to compare their pause times
 * <p>
 *     This class does nothing more but to create some garbage in order to allow for various different GCs to perform some collections and observe their pause times. The below GCs were tried out:
 *     <ul>
 *         <li><b>G1</b> mainly used as a benchmark</li>
 *         <li><b><a href="https://wiki.openjdk.java.net/display/zgc">ZGC</a></b></li>
 *         <li><b><a href="https://wiki.openjdk.java.net/display/shenandoah/Main">ShenandoahGC</a></b></li>
 *     </ul>
 * </p>
 * <h2>JVM OPTS</h2>
 * <p>
 *     The below JVM opts were used in all cases:
 *     {@code -Xms250M -Xmx250M -XX:-UseCompressedOops -XX:+UnlockExperimentalVMOptions -XX:+AlwaysPreTouch -XX:+UseNUMA -Xlog:gc:(zgc|shenandoah|g1).log -Xlog:gc*:(zgc|shenandoah|g1)All.log -XX:+Use(ZGC|ShenandoahGC|G1GC) -XX:+FlightRecorder -XX:StartFlightRecording=duration=120s,filename=(zgc|shenandoah|g1).jfr}
 * </p>
 */
public class VariousGCsNaiveRunner {

    private final Map<Integer, MutableIntHolder> histo = new ConcurrentHashMap<>();

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public VariousGCsNaiveRunner() {
        IntStream.range(0, 10).forEach(i -> histo.put(i, new MutableIntHolder()));
    }

    public void run() {
        while (true) {
            final String randomStr = String.valueOf(random.nextInt());
            int lastDigit = Integer.valueOf(randomStr.substring(randomStr.length() - 1));
            histo.compute(lastDigit, (k, v) -> v.increment());
        }
    }

    public String print() {
        final StringBuilder builder = new StringBuilder();
        histo.entrySet().stream().sorted(Comparator.comparingInt(Entry::getKey)).forEach(e -> builder.append(String.format("%d: %d\n", e.getKey(), e.getValue().value())));

        return builder.toString();
    }

    private static class MutableIntHolder {
        private Integer num = 0;// boxed object to allow for more garbage

        synchronized MutableIntHolder increment() {
            num++;
            return this;
        }

        synchronized Integer value() {
            return this.num;
        }
    }

    public static void main(final String... args) {
        final VariousGCsNaiveRunner zgc = new VariousGCsNaiveRunner();

        Executors.newSingleThreadExecutor().execute(zgc::run);

        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            System.out.println(zgc.print());
        }, 10, 10, TimeUnit.SECONDS);
    }
}
