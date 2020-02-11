package com.nikoskatsanos.benchmarks.singlevsmanythreads;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * <h1>Benchmark</h1>
 * A rather naive implementation of trying multiple threading strategies for a CPU based workload. The main goal is to check how much faster is running on a single thread, versus
 * fanning out the workload to multiple threads and also multiple threads using a thread-handover data structure (i.e. queue)
 *
 * <h1>Results:</h1>
 *  <h3>Setup:</h3>
 *      CPU: intel i5
 *      Memory: DDR3 8GB
 *      OS: Macos Mojave 10.14.6
 * <pre>
 *     {@code
 *      Benchmark                                                     Mode  Cnt      Score      Error   Units
 *      SingleVsManyThreadsBenchmark.blockingSetBitCalculator        thrpt    5     ≈ 10⁻⁴             ops/ns
 *      SingleVsManyThreadsBenchmark.multiThreadedSetBitCalculator   thrpt    5      0.001 ±    0.001  ops/ns
 *      SingleVsManyThreadsBenchmark.singleThreadedSetBitCalculator  thrpt    5      0.008 ±    0.002  ops/ns
 *      SingleVsManyThreadsBenchmark.blockingSetBitCalculator         avgt    5  11309.077 ± 1990.691   ns/op
 *      SingleVsManyThreadsBenchmark.multiThreadedSetBitCalculator    avgt    5   1699.693 ± 1400.355   ns/op
 *      SingleVsManyThreadsBenchmark.singleThreadedSetBitCalculator   avgt    5    125.122 ±    6.263   ns/op
 *     }
 * </pre>
 */
@State(Scope.Benchmark)
public class SingleVsManyThreadsBenchmark {

    private SetBitsCountStrategy singleThread = new SingleThreadActor();
    private SetBitsCountStrategy multiThreaded = new MultiThreadedActor();
    private SetBitsCountStrategy blocking = new BlockingQueueBackedActor();

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void singleThreadedSetBitCalculator(final Blackhole blackhole) {
        this.singleThread.calc(System.currentTimeMillis());
        blackhole.consume(this.singleThread.getEvenCount());
        blackhole.consume(this.singleThread.getOddCount());
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void multiThreadedSetBitCalculator(final Blackhole blackhole) {
        this.multiThreaded.calc(System.currentTimeMillis());
        blackhole.consume(this.multiThreaded.getEvenCount());
        blackhole.consume(this.multiThreaded.getOddCount());
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void blockingSetBitCalculator(final Blackhole blackhole) {
        this.blocking.calc(System.currentTimeMillis());
        blackhole.consume(this.blocking.getEvenCount());
        blackhole.consume(this.blocking.getOddCount());
    }

    public static void main(final String... args) throws CommandLineOptionException, RunnerException {
        final Options options = new OptionsBuilder()
            .parent(new CommandLineOptions(args))
            .include(SingleVsManyThreadsBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(3)
            .measurementIterations(5)
            .jvmArgsAppend(
                "-Xbatch",
                "-XX:-TieredCompilation",
                "-XX:+UnlockDiagnosticVMOptions"
            )
            .build();

        new Runner(options).run();
    }

}
