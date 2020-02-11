package com.nikoskatsanos.benchmarks.singlevsmanythreads;

public interface SetBitsCountStrategy {

    void calc(final long number);

    int getEvenCount();

    int getOddCount();
}
