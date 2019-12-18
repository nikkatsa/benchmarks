package com.nikoskatsanos.benchmarks.switchvsmap;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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
 * <h2>Benchmark</h2>
 * <p>
 *     The benchmark's intention is to test which approach is better when effectively having to switch between a finite number of values. The approaches that are often used are:
 *     <ul>
 *         <li>A switch statement</li>
 *         <li>An if-else statement</li>
 *         <li>Prepopulating the values into a map</li>
 *         <li>Prepopulating the values into an {@link EnumMap}, in case the input value is an enum</li>
 *     </ul>
 *
 *     The benchmark tests both a randomized input on the entire range of the possible finite values and also an input which stays constant throughout the execution (i.e. a common scenario).
 *
 *     The universe of possible values is ten, so that is ensured {@code switch} is implemented as a {@code tableswithc} on the byte code level (not at JIT level). For more info
 *     see <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-3.html#jvms-3.10">JVM Switch Spec</a>
 * </p>
 * <h2>Results:</h2>
 *    <h3>Setup:</h3>
 *    CPU: intel i5
 *    Memory: DDR3 8GB
 *    OS: Macos Mojave 10.14.6
 *    
 * <pre>
 *     {@code
 *      Benchmark                                  Mode  Cnt   Score   Error   Units
 *      SwitchVsMap.enumMapConstantInput          thrpt   10   0.171 ± 0.020  ops/ns
 *      SwitchVsMap.enumMapRandomInput            thrpt   10   0.090 ± 0.018  ops/ns
 *      SwitchVsMap.hashMapConstantInput          thrpt   10   0.131 ± 0.012  ops/ns
 *      SwitchVsMap.hashMapRandomInput            thrpt   10   0.049 ± 0.002  ops/ns
 *      SwitchVsMap.switchStatementConstantInput  thrpt   10   0.191 ± 0.015  ops/ns
 *      SwitchVsMap.switchStatementRandomInput    thrpt   10   0.032 ± 0.003  ops/ns
 *      SwitchVsMap.enumMapConstantInput           avgt   10   4.939 ± 0.341   ns/op
 *      SwitchVsMap.enumMapRandomInput             avgt   10   9.797 ± 0.154   ns/op
 *      SwitchVsMap.hashMapConstantInput           avgt   10   7.320 ± 0.098   ns/op
 *      SwitchVsMap.hashMapRandomInput             avgt   10  19.614 ± 0.500   ns/op
 *      SwitchVsMap.switchStatementConstantInput   avgt   10   5.081 ± 0.172   ns/op
 *      SwitchVsMap.switchStatementRandomInput     avgt   10  28.090 ± 0.500   ns/op
 *     }
 * </pre>
 */
@State(Scope.Benchmark)
public class SwitchVsMap {

    public enum Values {
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE
    }

    private Values[] vals = Values.values();

    private final Map<Values, String> valToStr = new HashMap<Values, String>() {
        {
            put(Values.ZERO, "0");
            put(Values.ONE, "1");
            put(Values.TWO, "2");
            put(Values.THREE, "3");
            put(Values.FOUR, "4");
            put(Values.FIVE, "5");
            put(Values.SIX, "6");
            put(Values.SEVEN, "7");
            put(Values.EIGHT, "8");
            put(Values.NINE, "9");
        }
    };

    private final EnumMap<Values, String> enumValToStr = new EnumMap<Values, String>(Values.class) {
        {
            put(Values.ZERO, "0");
            put(Values.ONE, "1");
            put(Values.TWO, "2");
            put(Values.THREE, "3");
            put(Values.FOUR, "4");
            put(Values.FIVE, "5");
            put(Values.SIX, "6");
            put(Values.SEVEN, "7");
            put(Values.EIGHT, "8");
            put(Values.NINE, "9");
        }
    };

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void switchStatementRandomInput(final Blackhole bh) {
        final Values value;
        switch (this.getRandom()) {
            case ZERO:
                value = Values.ZERO;
                break;
            case ONE:
                value = Values.ONE;
                break;
            case TWO:
                value = Values.TWO;
                break;
            case THREE:
                value = Values.THREE;
                break;
            case FOUR:
                value = Values.FOUR;
                break;
            case FIVE:
                value = Values.FIVE;
                break;
            case SIX:
                value = Values.SIX;
                break;
            case SEVEN:
                value = Values.SEVEN;
                break;
            case EIGHT:
                value = Values.EIGHT;
                break;
            case NINE:
                value = Values.NINE;
                break;
            default:
                throw new RuntimeException("Unexpected value");
        }

        bh.consume(value.toString());
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void switchStatementConstantInput(final Blackhole bh) {
        final Values value;
        switch (this.getConstant()) {
            case ZERO:
                value = Values.ZERO;
                break;
            case ONE:
                value = Values.ONE;
                break;
            case TWO:
                value = Values.TWO;
                break;
            case THREE:
                value = Values.THREE;
                break;
            case FOUR:
                value = Values.FOUR;
                break;
            case FIVE:
                value = Values.FIVE;
                break;
            case SIX:
                value = Values.SIX;
                break;
            case SEVEN:
                value = Values.SEVEN;
                break;
            case EIGHT:
                value = Values.EIGHT;
                break;
            case NINE:
                value = Values.NINE;
                break;
            default:
                throw new RuntimeException("Unexpected value");
        }

        bh.consume(value.toString());
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void hashMapRandomInput(final Blackhole bh) {
        final String value = this.valToStr.get(this.getRandom());
        bh.consume(value);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void hashMapConstantInput(final Blackhole bh) {
        final String value = this.valToStr.get(this.getConstant());
        bh.consume(value);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void enumMapRandomInput(final Blackhole bh) {
        final String value = this.enumValToStr.get(this.getRandom());
        bh.consume(value);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void enumMapConstantInput(final Blackhole bh) {
        final String value = this.enumValToStr.get(this.getConstant());
        bh.consume(value);
    }

    private Values getRandom() {
        return this.vals[ThreadLocalRandom.current().nextInt(0, 10)];
    }

    private Values getConstant() {
        return Values.FIVE;
    }

    public static void main(final String... args) throws RunnerException, CommandLineOptionException {
        final Options options = new OptionsBuilder()
            .parent(new CommandLineOptions(args))
            .include(SwitchVsMap.class.getSimpleName())
            .forks(1)
            .warmupIterations(5)
            .measurementIterations(10)
            .jvmArgsAppend(
                "-Xbatch",
                "-XX:-TieredCompilation",
                "-XX:+UnlockDiagnosticVMOptions"
            )
            .build();

        new Runner(options).run();
    }
}
