package com.bells.systems;

/**
 * Lab 1 — Stack Overflow
 *
 * Demonstrates: JVM thread stack exhaustion via unbounded recursion.
 * Each method frame is pushed onto the thread stack (-Xss controls its size, default ~512KB–1MB).
 * When the stack is full, the JVM throws StackOverflowError — a hard JVM error, not a Java exception.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass="com.bells.systems.Lab1_StackOverflow"
 *
 * Reproduce faster with a smaller stack:
 *   mvn exec:java -Dexec.mainClass="com.bells.systems.Lab1_StackOverflow" -Dexec.args="" \
 *     -Djvm.args="-Xss256k"
 * Or directly:
 *   java -Xss256k -cp target/classes com.bells.systems.Lab1_StackOverflow
 *
 * FIX: convert recursion to an iterative loop — no frames pile up.
 */
public class Lab1_StackOverflow {

    private static int depth = 0;

    // BROKEN: no base case — recurses until the thread stack is exhausted
    static void recurse() {
        depth++;
        recurse(); // each call pushes a new frame; stack space is finite
    }

    public static void main(String[] args) {
        System.out.println("=== Lab 1: Stack Overflow ===");
        System.out.println("Starting infinite recursion...");
        System.out.println("JVM default thread stack: ~512 KB – 1 MB (controlled by -Xss)");
        System.out.println();

        try {
            recurse();
        } catch (StackOverflowError e) {
            System.out.println("*** StackOverflowError caught ***");
            System.out.printf("  Reached recursion depth: %,d frames%n", depth);
            System.out.println();
            System.out.println("Root cause:");
            System.out.println("  Every method call pushes a NEW stack frame (local vars, return address,");
            System.out.println("  operand stack). With no base case the frames never pop.");
            System.out.println("  The JVM kills the thread once -Xss bytes are exhausted.");
            System.out.println();
            System.out.println("The -Xss flag:");
            System.out.println("  -Xss256k  -> ~1,000–4,000 frames  (small; triggers faster)");
            System.out.println("  -Xss1m    -> ~8,000–15,000 frames (JVM default on most platforms)");
            System.out.println("  -Xss8m    -> ~70,000+ frames      (useful for deep legitimate recursion)");
            System.out.println();
            System.out.println("Fix: rewrite recurse() as an iterative loop — O(1) stack, O(n) heap.");
        }
    }
}
