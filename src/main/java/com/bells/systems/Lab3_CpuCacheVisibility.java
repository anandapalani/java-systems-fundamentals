package com.bells.systems;

/**
 * Lab 3 — CPU Cache Visibility (missing volatile)
 *
 * Demonstrates: L1/L2 CPU cache isolation between cores.
 * Modern CPUs keep per-core caches. Without a memory barrier, a write on Core A
 * may sit in Core A's L1 cache and never reach Core B's view of the variable.
 * The JVM's JIT compiler can also hoist the flag read out of the loop entirely
 * ("loop-invariant code motion"), making the worker spin forever.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass="com.bells.systems.Lab3_CpuCacheVisibility"
 *
 * Expected failure: worker thread ignores the flag update and keeps spinning.
 *
 * FIX: declare 'active' as:  private static volatile boolean active = true;
 *      volatile inserts a happens-before barrier — any write is flushed to
 *      main memory before any subsequent read on another thread sees it.
 */
public class Lab3_CpuCacheVisibility {

    // BROKEN: non-volatile — writes may stay in the writing core's L1 cache
    private static boolean active = true;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Lab 3: CPU Cache Visibility ===");
        System.out.println("Worker thread starts a tight while(active) loop.");
        System.out.println("Main thread will set active=false after 400 ms.");
        System.out.println();
        System.out.println("Without 'volatile': the write may be invisible to the worker core.");
        System.out.println("  JIT can cache 'active' in a register (loop-invariant hoisting).");
        System.out.println("  CPU L1 cache may not flush to shared L3 / main memory.");
        System.out.println();

        Thread worker = new Thread(() -> {
            long iterations = 0;
            while (active) { // tight loop — JIT will optimise this aggressively
                iterations++;
            }
            System.out.println("  Worker exited after " + iterations + " iterations.");
        }, "worker-thread");

        worker.setDaemon(true); // allow JVM to exit even if worker is stuck
        worker.start();

        Thread.sleep(400); // give the JIT time to compile and cache-optimise the loop
        System.out.println("Main: setting active = false ...");
        active = false;
        System.out.println("Main: waiting up to 2 seconds for worker to exit...");

        worker.join(2000);

        if (worker.isAlive()) {
            System.out.println();
            System.out.println("*** TRAP SPRUNG: worker is STILL ALIVE after 2 seconds! ***");
            System.out.println();
            System.out.println("Root cause:");
            System.out.println("  Without 'volatile', there is NO happens-before guarantee.");
            System.out.println("  The JIT compiled the while(active) check using a register-cached copy.");
            System.out.println("  The main thread's write landed in its L1/L2 cache and was never");
            System.out.println("  propagated to the worker's core — so the worker loops forever.");
            System.out.println();
            System.out.println("CPU memory hierarchy recap:");
            System.out.println("  L1 cache:  ~4 cycles,  per-core private (~32 KB)");
            System.out.println("  L2 cache:  ~12 cycles, per-core private (~256 KB)");
            System.out.println("  L3 cache:  ~40 cycles, shared across cores (~8–32 MB)");
            System.out.println("  Main RAM:  ~200 cycles, fully visible to all cores");
            System.out.println();
            System.out.println("Fix: add 'volatile' keyword to 'active'.");
            System.out.println("  volatile guarantees:");
            System.out.println("    1. Every write is flushed to main memory immediately.");
            System.out.println("    2. Every read bypasses the cache and fetches from main memory.");
            System.out.println("    3. Establishes a happens-before relationship (JMM §17.4.5).");
            System.out.println();
            System.out.println("  Alternatively use AtomicBoolean (also appropriate for compound actions).");
            worker.interrupt(); // clean up the stuck daemon thread
        } else {
            System.out.println();
            System.out.println("Worker exited cleanly — the flag was visible on this run.");
            System.out.println("Note: this outcome is non-deterministic without volatile.");
            System.out.println("The JVM / JIT / OS are free to re-order or cache the read.");
            System.out.println("Add 'volatile' to guarantee visibility on every platform and JVM version.");
        }

        System.out.println();
        System.out.println("Lab 3 complete.");
    }
}
