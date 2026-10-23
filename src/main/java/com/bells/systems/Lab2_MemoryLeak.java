package com.bells.systems;

import java.util.ArrayList;
import java.util.List;

/**
 * Lab 2 — Memory Leak / OutOfMemoryError
 *
 * Demonstrates: GC root retention via a static reference chain.
 * A static field is a GC root — the garbage collector can NEVER collect objects
 * reachable from it, even if the rest of the application has forgotten them.
 *
 * Run with a small heap to see the OOM quickly:
 *   java -Xmx128m -cp target/classes com.bells.systems.Lab2_MemoryLeak
 * Or via Maven (add JVM flags with MAVEN_OPTS):
 *   MAVEN_OPTS="-Xmx128m" mvn exec:java -Dexec.mainClass="com.bells.systems.Lab2_MemoryLeak"
 *
 * FIX: don't use static collections as unbounded caches;
 *      use WeakReference / SoftReference, or cap the list and evict old entries.
 */
public class Lab2_MemoryLeak {

    // BROKEN: static → GC root → everything appended here lives forever
    private static final List<byte[]> LEAK = new ArrayList<>();

    private static final int CHUNK_SIZE = 10 * 1024 * 1024; // 10 MB per chunk

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Lab 2: Memory Leak / OutOfMemoryError ===");
        System.out.println("Allocating 10 MB chunks and appending to a static List...");
        System.out.println("Run with:  java -Xmx128m -cp target/classes com.bells.systems.Lab2_MemoryLeak");
        System.out.println();

        Runtime rt = Runtime.getRuntime();
        int iteration = 0;

        try {
            while (true) {
                iteration++;
                byte[] chunk = new byte[CHUNK_SIZE];
                LEAK.add(chunk); // GC root chain keeps every chunk alive

                long usedMB  = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
                long totalMB = rt.totalMemory() / (1024 * 1024);
                long maxMB   = rt.maxMemory()   / (1024 * 1024);

                System.out.printf("  Iteration %3d | used: %4d MB / total: %4d MB / max: %4d MB | chunks retained: %d%n",
                        iteration, usedMB, totalMB, maxMB, LEAK.size());

                Thread.sleep(100); // slow down output so the pattern is readable
            }
        } catch (OutOfMemoryError e) {
            System.out.println();
            System.out.println("*** java.lang.OutOfMemoryError: Java heap space ***");
            System.out.printf("  Leaked %d chunks × 10 MB = ~%d MB before OOM%n",
                    LEAK.size(), LEAK.size() * 10L);
            System.out.println();
            System.out.println("Root cause:");
            System.out.println("  'LEAK' is static → it is a GC root.");
            System.out.println("  Every byte[] appended to it is strongly reachable → GC cannot collect any.");
            System.out.println("  Heap fills up → JVM throws OutOfMemoryError.");
            System.out.println();
            System.out.println("Metaspace note:");
            System.out.println("  Class metadata lives in Metaspace (off-heap). If you load classes dynamically");
            System.out.println("  (e.g. with custom ClassLoaders) without releasing the loader, Metaspace fills up");
            System.out.println("  instead: OutOfMemoryError: Metaspace. Cap with -XX:MaxMetaspaceSize=128m.");
            System.out.println();
            System.out.println("Fix: use a bounded cache (LinkedHashMap with removeEldestEntry),");
            System.out.println("     WeakHashMap, or SoftReference<byte[]> so the GC can reclaim under pressure.");
        }
    }
}
