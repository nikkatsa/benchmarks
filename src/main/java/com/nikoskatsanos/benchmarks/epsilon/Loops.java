package com.nikoskatsanos.benchmarks.epsilon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <h1>Allocation while looping</h1>
 *
 * <h2>Scenarios</h2>
 * <p>
 * This is a small experiment to determine which for <b>for loops</b> perform allocation of objects. Based on
 * <a href="https://docs.oracle.com/javase/specs/jls/se13/html/jls-14.html#jls-14.14.2">Java Language Specification</a> the <b>enhanced for loop</b> has different semantics based
 * on if the iterated entity is a collection (i.e. {@link Iterable}) or a an array.
 *
 * The below scenarios were tested:
 * <ul>
 * <li>for loop over an array</li>
 * <li>enhanced for loop over an array</li>
 * <li>enhanced for loop over an array with auto-boxing</li>
 * <li>for loop over a collection ({@link ArrayList}</li>
 * <li>enhanced for loop over a collection ({@link ArrayList}</li>
 * <li>enhanced for loop with auto-boxing over a collection ({@link ArrayList})</li>
 * <li>for loop using the {@link Iterator} of a collection ({@link ArrayList})</li>
 * </ul>
 * </p>
 *
 * <h2>Setup</h2>
 *
 * <p>
 * The experiment was run in the below setup:
 * <ul>
 * <li><b>OS:</b> MacOS Catalina (10.15.3), Core i5 @2.6Hz, 8GB DDR3</li>
 * <li><b>JDK:</b> openjdk version "13.0.2" 2020-01-14</li>
 * <li><b>VM OPTS</b> {@code -Xms512M -Xmx512M -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC [-XX:-DoEscapeAnalysis]}</li>
 * </ul>
 * </p>
 *
 * <h2>Results</h2>
 * A short commentary on the results seen. It is important to note that results were different with and without VM option {@code -XX:-DoEscapeAnalysis}, as by having {@code
 * EscapeAnalysis} enabled was forcing the allocations to take place on the stack, rather than the heap
 * <h3>For Loop Over Array</h3>
 * <p>
 * As expected this scenario did not produce any allocations/garbage
 * </p>
 *
 * <h3>Enhanced For Loop Over an Array</h3>
 * <p>
 * Following the <b>Java Language Specification</b> for an enhanced-for loop over and array, as expected this scenario did not produce any allocations/garbage
 * </p>
 *
 * <h3>Enhanced For Loop Over an Array With Auto-boxing</h3>
 * <p>
 * As expected, the autoboxing was causing new objects to get created, hence increase in used memory was observed. This happenned regardless of {@code -XX:-DoEscapeAnalysis}
 * </p>
 *
 * <h3>For Loop Over a Collection ({@link ArrayList})</h3>
 * <p>
 * As expected this scenario did not produce any allocations/garbage
 * </p>
 *
 * <h3>Enhanced For Loop Over a Collection ({@link ArrayList})</h3>
 * <p>
 * This scenario produced new allocations/garbage if run with {@code -XX:-DoEscapeAnalysis}), as an enhanced-for loop for a collection is using the {@link Iterator} of that
 * collection, and {@link ArrayList#iterator()} is creating a new object on every call (<a href="https://github.com/openjdk/jdk/blob/78c4ab44496b2ec7ff0ffd8746dd31a17b784dd3/src/java.base/share/classes/java/util/ArrayList.java#L945">ArrayList#iterator</a>.
 * It did not though produce any on heap allocations if <b>EscapeAnalysis</b> was enabled
 * </p>
 *
 * <h3>For Loop Using Iterator</h3>
 * <p>
 * This scenario was mimicing the unroll of the enhanced-for loop over a collection, based on <b>Java Language Specification</b>. As seen the results were exactly the same as in an
 * enhanced-for loop. Allocations/Garbage were observed if {@code -XX:-DoEscapeAnalysis}.
 * </p>
 *
 * <h3>Enhanced For Loop Over a Collection ({@link ArrayList}) with Autoboxing</h3>
 * <p>
 * This scenario as expected produced lots of allocations due to the {@link Iterator} objects and also the auto-boxing of the primitive values
 * </p>
 */
public class Loops {

    private static final int MB = 1024 * 1024;
    private static final int[] VALUES = new int[100_000];
    private static final List<Integer> LIST_VALUES = new ArrayList<>(100_000);

    static {
        for (int i = 0; i < VALUES.length; i++) {
            VALUES[i] = i;
            LIST_VALUES.add(i);
        }
    }

    private static long forLoopArray() {
        long sum = 0;
        for (int i = 0; i < VALUES.length; i++) {
            sum += VALUES[i];
        }
        return sum;
    }

    private static long forEachLoopArray() {
        long sum = 0;
        for (int val : VALUES) {
            sum += val;
        }
        return sum;
    }

    private static long forEachBoxingLoopArray() {
        long sum = 0;
        for (Integer val : VALUES) {
            sum += val;
        }
        return sum;
    }

    private static long forLoopList() {
        long sum = 0;
        for (int i = 0; i < LIST_VALUES.size(); i++) {
            sum += LIST_VALUES.get(i);
        }
        return sum;
    }

    private static long forEachLoopList() {
        long sum = 0;
        for (int val : LIST_VALUES) {
            sum += val;
        }
        return sum;
    }

    private static long forEachLoopListIterator() {
        long sum = 0;
        for (Iterator<Integer> it = LIST_VALUES.iterator(); it.hasNext(); ) {
            sum += it.next();
        }
        return sum;
    }

    private static long forEachBoxingLoopList() {
        long sum = 0;
        for (Integer val : LIST_VALUES) {
            sum += val;
        }
        return sum;
    }

    public static void main(final String... args) {
        long usedKB = getUsedMemory();
        System.out.println("Initial UsedMemory: " + usedKB + "MB");

        long sum = 0;
        for (int i = 0; i < 1_000_000; i++) {

            // Arrays
//            long newSum = forLoopArray();
//            long newSum = forEachLoopArray();
//            long newSum = forEachBoxingLoopArray();

            // Collections
//            long newSum = forLoopList();
            long newSum = forEachLoopList();
//            long newSum = forEachLoopListIterator();
//            long newSum = forEachBoxingLoopList();

            // if-block making sure the result of the iteration is used, so it does not get optimized out by JIT
            if (i == 0) {
                sum = newSum;
            } else if (sum != newSum) {
                System.err.println(String.format("Wrong sum. Expected: %d, Actual: %d", sum, newSum));
            }

            if (i % 1000 == 0) {
                long newUsedKB = getUsedMemory();
                if (usedKB != newUsedKB) {
                    System.out.println("UsedMemory: " + newUsedKB + "MB");
                    usedKB = getUsedMemory();
                }
            }
        }

        System.out.println("Final UsedMemory: " + getUsedMemory() + "MB");
    }

    private static long getUsedMemory() {
        return (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / MB;
    }
}
