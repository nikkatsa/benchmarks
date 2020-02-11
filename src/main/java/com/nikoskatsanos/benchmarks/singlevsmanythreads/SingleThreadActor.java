package com.nikoskatsanos.benchmarks.singlevsmanythreads;

public class SingleThreadActor implements SetBitsCountStrategy {

    private int evenCount = 0;
    private int oddCount = 0;

    @Override
    public void calc(final long number) {
        final short setBits = SetBitsCalculator.countSetBits(number);

        if (setBits % 2 == 0) {
            evenCount++;
        } else {
            oddCount++;
        }
    }

    @Override
    public int getEvenCount() {
        return this.evenCount;
    }

    @Override
    public int getOddCount() {
        return this.oddCount;
    }


}
