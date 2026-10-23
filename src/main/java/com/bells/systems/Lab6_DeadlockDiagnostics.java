package com.bells.systems;

/**
 * Lab 6 — Deadlock Diagnostics
 *
 * Demonstrates: circular lock acquisition → permanent deadlock.
 * Thread 1 acquires lock A, then tries to acquire lock B.
 * Thread 2 acquires lock B, then tries to acquire lock A.
 * Neither can proceed — classic circular wait (one of Coffman's four conditions).
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass="com.bells.systems.Lab6_DeadlockDiagnostics"
 *
 * Program will HANG. Note the PID printed to console, then in a second terminal:
 *   jstack <PID>          # prints full thread dump with deadlock cycle
 *   jcmd  <PID> Thread.print  # alternative to jstack
 *
 * Look for the section:  "Found one Java-level deadlock:"
 *
 * To capture the thread dump non-interactively:
 *   kill -3 <PID>         # sends SIGQUIT → JVM prints thread dump to stdout
 *
 * FIX: always acquire locks in a CONSISTENT, globally-agreed order.
 *      e.g. sort locks by System.identityHashCode() before acquiring.
 *      Or use java.util.concurrent.locks.Lock with tryLock(timeout) to detect and back off.
 */
public class Lab6_DeadlockDiagnostics {

    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) throws InterruptedException {
        long pid = ProcessHandle.current().pid();
        System.out.println("=== Lab 6: Deadlock Diagnostics ===");
        System.out.println("Process PID: " + pid);
        System.out.println();
        System.out.println("Two threads will acquire two locks in OPPOSITE orders:");
        System.out.println("  Thread-1: acquires LOCK_A, then tries LOCK_B");
        System.out.println("  Thread-2: acquires LOCK_B, then tries LOCK_A");
        System.out.println();
        System.out.println("The program will HANG in a circular deadlock.");
        System.out.println("In a second terminal, inspect the deadlock with:");
        System.out.println("  jstack " + pid);
        System.out.println("  jcmd   " + pid + " Thread.print");
        System.out.println("  kill -3 " + pid + "  (SIGQUIT → thread dump to stdout)");
        System.out.println();
        System.out.println("Look for: \"Found one Java-level deadlock:\" in the jstack output.");
        System.out.println();

        Thread thread1 = new Thread(() -> {
            System.out.println("Thread-1: waiting for LOCK_A...");
            synchronized (LOCK_A) {
                System.out.println("Thread-1: acquired LOCK_A. Sleeping 200ms before trying LOCK_B...");
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}

                System.out.println("Thread-1: trying to acquire LOCK_B... (will block here)");
                synchronized (LOCK_B) {
                    // DEADLOCK: Thread-2 holds LOCK_B and is waiting for LOCK_A
                    System.out.println("Thread-1: acquired LOCK_B. (this line never prints)");
                }
            }
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            System.out.println("Thread-2: waiting for LOCK_B...");
            synchronized (LOCK_B) {
                System.out.println("Thread-2: acquired LOCK_B. Sleeping 200ms before trying LOCK_A...");
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}

                System.out.println("Thread-2: trying to acquire LOCK_A... (will block here)");
                synchronized (LOCK_A) {
                    // DEADLOCK: Thread-1 holds LOCK_A and is waiting for LOCK_B
                    System.out.println("Thread-2: acquired LOCK_A. (this line never prints)");
                }
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();

        // Main thread waits — the JVM will hang here indefinitely
        thread1.join();
        thread2.join();

        // This line is unreachable in the deadlock scenario
        System.out.println("Both threads finished. (unreachable without a fix)");
    }
}
