package com.bells.systems;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lab 4 — Race Condition vs. Atomic CAS
 *
 * Demonstrates: why unsynchronised read-modify-write is not atomic.
 * counter++ compiles to THREE bytecode instructions:
 *   getfield   (read)
 *   iconst_1 / iadd  (add)
 *   putfield   (write)
 * Two threads can interleave between any of these steps, causing lost updates.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass="com.bells.systems.Lab4_RaceCondition"
 *
 * Disassemble to see the 3-step bytecode:
 *   javap -c target/classes/com/bells/systems/Lab4_RaceCondition.class
 *
 * FIX: replace unsafeCount++ with AtomicInteger.incrementAndGet() which uses
 *      a single hardware CAS (Compare-And-Swap) instruction — atomic by CPU design.
 */
public class Lab4_RaceCondition {

    private static int          unsafeCount = 0;        // BROKEN: plain int, no sync
    private static final AtomicInteger safeCount = new AtomicInteger(0); // FIXED

    private static final int INCREMENTS_PER_THREAD = 10_000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Lab 4: Race Condition vs. Atomic CAS ===");
        System.out.printf("Two threads each call counter++ %,d times.%n", INCREMENTS_PER_THREAD);
        System.out.printf("Expected total: %,d%n%n", INCREMENTS_PER_THREAD * 2);

        // --- Unsafe run ---
        unsafeCount = 0;
        Thread t1Unsafe = new Thread(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                unsafeCount++; // read → add → write: NOT atomic
            }
        }, "unsafe-t1");

        Thread t2Unsafe = new Thread(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                unsafeCount++; // races with t1Unsafe on every single iteration
            }
        }, "unsafe-t2");

        t1Unsafe.start();
        t2Unsafe.start();
        t1Unsafe.join();
        t2Unsafe.join();

        int unsafeFinal = unsafeCount;

        // --- Safe run (AtomicInteger) ---
        safeCount.set(0);
        Thread t1Safe = new Thread(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                safeCount.incrementAndGet(); // single CAS instruction — atomic
            }
        }, "safe-t1");

        Thread t2Safe = new Thread(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                safeCount.incrementAndGet();
            }
        }, "safe-t2");

        t1Safe.start();
        t2Safe.start();
        t1Safe.join();
        t2Safe.join();

        int safeFinal = safeCount.get();
        int expected  = INCREMENTS_PER_THREAD * 2;

        System.out.println("Results:");
        System.out.printf("  unsafe int++          : %,6d  (expected %,d) — lost updates: %,d%n",
                unsafeFinal, expected, expected - unsafeFinal);
        System.out.printf("  AtomicInteger.incAndGet: %,6d  (expected %,d) — lost updates: %,d%n",
                safeFinal, expected, expected - safeFinal);
        System.out.println();

        if (unsafeFinal < expected) {
            System.out.println("*** Race condition confirmed: lost " + (expected - unsafeFinal) + " increments ***");
        } else {
            System.out.println("(Both produced correct results this run — the race is non-deterministic.)");
            System.out.println("Run multiple times; you will observe lost updates.");
        }

        System.out.println();
        System.out.println("Why counter++ is NOT atomic:");
        System.out.println("  Bytecode (javap -c): getfield → iadd → putfield  (3 separate instructions)");
        System.out.println("  Thread A reads 42, Thread B reads 42, both add 1, both write 43.");
        System.out.println("  One increment is silently lost.");
        System.out.println();
        System.out.println("How AtomicInteger.incrementAndGet() works:");
        System.out.println("  Uses a single CPU LOCK XADD (x86) or STLXR+LDXR (ARM) instruction.");
        System.out.println("  The CPU guarantees read-modify-write happens atomically — no interleave possible.");
        System.out.println("  Internally implemented via sun.misc.Unsafe.compareAndSetInt (CAS loop).");
        System.out.println();
        System.out.println("Run: javap -c target/classes/com/bells/systems/Lab4_RaceCondition.class");
        System.out.println("     Look for 'getfield', 'iadd', 'putfield' sequence in the unsafe path.");
    }
}
