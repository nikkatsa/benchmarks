package com.nikoskatsanos.benchmarks.loops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * Measuring invocation of an object's method, directly and having the object in a one element data container
 * <h1>Background</h1>
 * <p>
 *      In a few/lot of cases we find ourselves storing objects in a data container (i.e. List, Set, Array etc) and looping through it, in order to invoke a method of each element.
 *      This particularly happens in use cases of Event listeners, when a callback needs to be made. In such scenarios, usually there is an API to attach/add a listener to an
 *      object and a loop around all listeners by invoking their callback method.
 *
 *      Howevever, it is not uncommon that <b>only one</b> listener be attached/added. Hence a cost will be paid for the iteration and fetching of the single element in order to
 *      invoke its method, in comparison with having the instance of the specified object in an instnace field and directly invoking its method
 * </p>
 * <h1>Benchmark</h1>
 * <p>
 *     This simple benchmark tries to measure the overhead that is been paid in sitatuations like that. The benchmark does the following:
 *     <ul>
 *         <li>Invokes an object's method direclty</li>
 *         <li>Adds the object in an {@link ArrayList} and loops over and invokes its method</li>
 *         <li>Adds the object in an {@code array} and loops over and invokes its method</li>
 *         <li>Adds the object in an {@link HashSet} and loops over and invokes its method</li>
 *     </ul>
 *
 *     The benchmark measures both the throughput (ops/ns) and the latency (ns/ops) of the invocations, in order to determine the extra overhead
 * </p>
 * <h1>Results</h1>
 * <p>
 *     Below are the results for each of the benchmarks (MacOS 10.14.6, 2.6GHz Intel i5, 8GB DDR3)
 *     <pre>
 *         {@code
 *         Benchmark                                                          Mode  Cnt   Score   Error   Units
 *          SingleElementLoopBenchmark.directInvocation                       thrpt   10   0.317 ± 0.022  ops/ns
 *          SingleElementLoopBenchmark.singleElementArrayLoopInvocation       thrpt   10   0.155 ± 0.028  ops/ns
 *          SingleElementLoopBenchmark.singleElementListLoopInvocation        thrpt   10   0.114 ± 0.010  ops/ns
 *          SingleElementLoopBenchmark.singleElementSetLoopInvocation         thrpt   10   0.016 ± 0.002  ops/ns
 *          SingleElementLoopBenchmark.directInvocation                        avgt   10   3.430 ± 0.172   ns/op
 *          SingleElementLoopBenchmark.singleElementArrayLoopInvocation        avgt   10   6.125 ± 0.422   ns/op
 *          SingleElementLoopBenchmark.singleElementListLoopInvocation         avgt   10   9.101 ± 2.089   ns/op
 *          SingleElementLoopBenchmark.singleElementSetLoopInvocation          avgt   10  59.008 ± 1.747   ns/op
 *         }
 *     </pre>
 *
 *     As seen, adding the element in a data container with just one element, has quite a significant impact. That extra cost is mainly attributed to the fact that the object will
 *     need to be fetched from inside the data container, along with some extra checks (i.e. size check, type check etc).
 *
 *     A brief detailed explanation can be seen in this Stack Overflow question:
 *     <a href="https://stackoverflow.com/questions/58477651/java-method-direct-invocation-vs-single-element-loop-invocation/58477990#58477990">Java method direct invocation vs single element loop invocation</a>
 * </p>
 */
@State(Scope.Benchmark)
public class SingleElementLoopBenchmark {

    private final Dispatcher dispatcher = new Dispatcher();

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void directInvocation(final Blackhole blackhole) {
        this.dispatcher.invoke(blackhole);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void singleElementListLoopInvocation(final Blackhole blackhole) {
        this.dispatcher.invokeInListLoop(blackhole);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void singleElementArrayLoopInvocation(final Blackhole blackhole) {
        this.dispatcher.invokeInArrayLoop(blackhole);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void singleElementSetLoopInvocation(final Blackhole blackhole) {
        this.dispatcher.invokeInSetLoop(blackhole);
    }

    @State(Scope.Benchmark)
    public static class Dispatcher {

        private final Listener listener = new Listener();

        private final List<Listener> singleListenerList = new ArrayList<Listener>() {{
            add(listener);
        }};

        private final Listener[] singleListenerArray = new Listener[]{listener};

        private final Set<Listener> singleListenerSet = new HashSet<Listener>() {{
            add(listener);
        }};

        public void invoke(final Blackhole blackhole) {
            this.listener.performAction(blackhole);
        }

        public void invokeInListLoop(final Blackhole blackhole) {
            for (int i = 0; i < this.singleListenerList.size(); i++) {
                this.singleListenerList.get(i).performAction(blackhole);
            }
        }

        public void invokeInArrayLoop(final Blackhole blackhole) {
            for (int i = 0; i < this.singleListenerArray.length; i++) {
                this.singleListenerArray[i].performAction(blackhole);
            }
        }

        public void invokeInSetLoop(final Blackhole blackhole) {
            for (final Listener listener : this.singleListenerSet) {
                listener.performAction(blackhole);
            }
        }
    }

    public static class Listener {

        void performAction(final Blackhole blackhole) {
            blackhole.consume(true);
        }
    }

    public static void main(String... args) throws RunnerException, CommandLineOptionException {
        args = new String[]{"-prof", "dtraceasm", "hotThreshold", "0.02"};
        final Options options = new OptionsBuilder()
            .parent(new CommandLineOptions(args))
            .include(SingleElementLoopBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(5)
            .measurementIterations(10)
            .jvmArgsAppend(
                "-Xbatch",
                "-XX:-TieredCompilation",
                "-XX:+PrintCompilation",
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+LogCompilation",
                "-XX:LogFile=compiler.out",
                "-XX:+PrintAssembly",
                "-XX:+PrintInterpreter",
                "-XX:+PrintNMethods",
                "-XX:+PrintNativeNMethods",
                "-XX:+PrintSignatureHandlers",
                "-XX:+PrintAdapterHandlers",
                "-XX:+PrintStubCode",
                "-XX:+PrintCompilation",
                "-XX:+PrintInlining",
                "-XX:+TraceClassLoading",
                "-XX:PrintAssemblyOptions=syntax"
            )
            .build();

        new Runner(options).run();
    }
}
