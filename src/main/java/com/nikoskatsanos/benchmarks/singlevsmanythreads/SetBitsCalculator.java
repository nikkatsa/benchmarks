package com.nikoskatsanos.benchmarks.singlevsmanythreads;

public class SetBitsCalculator {

    private SetBitsCalculator() {
    }

    public static short countSetBits(final long number) {
        short setBits = 0;
        for (int i = 0; i < 64; i++) {
            final long mask = 1L << i;

            if (((number & mask) >> i) == 1) {
                setBits++;
            }
        }
        return setBits;
    }
}
