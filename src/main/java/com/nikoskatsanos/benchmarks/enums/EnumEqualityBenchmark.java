package com.nikoskatsanos.benchmarks.enums;

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
 * <h1>Test:</h1>
 * Enums can be tested for equality either by invoking the {@link Enum#equals(Object)} method, or directly by reference equality, as effectively they are singletons.
 * The {@link Enum#equals(Object)} method is just invoking the reference equality under the cover. Hence one would expect the two to be equivalent in terms of performance
 *
 * <h1>Test Results:</h1>
 *  <pre>
 *      {@code
 *      Benchmark                                 Mode  Cnt   Score    Error   Units
 *      EnumEqualityBenchmark.equalsMethod       thrpt   10   0.060 ±  0.001  ops/ns
 *      EnumEqualityBenchmark.referenceEquality  thrpt   10   0.059 ±  0.001  ops/ns
 *      EnumEqualityBenchmark.equalsMethod        avgt   10  16.756 ±  0.051   ns/op
 *      EnumEqualityBenchmark.referenceEquality   avgt   10  16.840 ±  0.152   ns/op
 *      }
 *  </pre>
 *
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class EnumEqualityBenchmark {

    enum Dummy {
        ONE, TWO, THREE, FOUR, FIVE
    }

    private int idx = 0;

    private int next() {
        if (idx % Dummy.values().length-1 > 0) {
            idx=0;
        };
        return idx++;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    public void equalsMethod(final Blackhole blackhole) {
        blackhole.consume(Dummy.ONE.equals(Dummy.values()[this.next()]));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    public void referenceEquality(final Blackhole blackhole) {
        blackhole.consume(Dummy.ONE == Dummy.values()[this.next()]);
    }

    public static void main(final String... args) throws CommandLineOptionException, RunnerException {
        final Options options = new OptionsBuilder()
            .parent(new CommandLineOptions(args))
            .include(EnumEqualityBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(5)
            .measurementIterations(10)
            .jvmArgsAppend(
                "-Xbatch",
                "-XX:-TieredCompilation",
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+LogCompilation",
                "-XX:LogFile=compiler.out",
                "-XX:PrintAssemblyOptions=syntax"
            )
            .build();

        new Runner(options).run();
    }
}
