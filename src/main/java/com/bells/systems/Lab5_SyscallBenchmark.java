package com.bells.systems;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lab 5 — Syscall Benchmark: Unbuffered vs. Buffered I/O
 *
 * Demonstrates: the cost of user-space → kernel-space context switches.
 * Every FileInputStream.read() for a single byte issues one read(2) syscall.
 * A 5 MB file = 5,000,000 syscalls. Each syscall involves:
 *   1. CPU mode switch: user → kernel (saves registers, switches page table)
 *   2. Kernel validates args, copies data, updates file offset
 *   3. CPU mode switch: kernel → user (restores registers)
 * Cost: ~100–1000 ns per syscall → seconds of overhead for millions of them.
 *
 * BufferedInputStream batches reads into 8 KB blocks, reducing syscalls to ~610.
 *
 * Run:
 *   mvn exec:java -Dexec.mainClass="com.bells.systems.Lab5_SyscallBenchmark"
 */
public class Lab5_SyscallBenchmark {

    private static final int FILE_SIZE_MB  = 5;
    private static final int FILE_SIZE     = FILE_SIZE_MB * 1024 * 1024; // 5,242,880 bytes
    private static final int BUFFER_SIZE   = 8 * 1024;                   // 8 KB

    public static void main(String[] args) throws IOException {
        System.out.println("=== Lab 5: Syscall Benchmark — Unbuffered vs. Buffered I/O ===");
        System.out.printf("File size: %d MB (%,d bytes)%n%n", FILE_SIZE_MB, FILE_SIZE);

        // Create a temporary 5 MB file filled with sequential bytes
        Path tempFile = Files.createTempFile("lab5_benchmark_", ".dat");
        System.out.println("Creating temp file: " + tempFile);
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            byte[] block = new byte[64 * 1024]; // write in 64 KB blocks for speed
            int written = 0;
            while (written < FILE_SIZE) {
                int toWrite = Math.min(block.length, FILE_SIZE - written);
                fos.write(block, 0, toWrite);
                written += toWrite;
            }
        }
        System.out.println("Temp file created.\n");

        // --- Benchmark 1: Unbuffered (1 byte at a time) ---
        System.out.println("Benchmark 1: Unbuffered FileInputStream.read() — 1 byte per call");
        System.out.printf("  Expected syscalls: ~%,d (one read(2) per byte)%n", FILE_SIZE);

        long startUnbuffered = System.currentTimeMillis();
        long bytesRead1 = 0;
        try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
            while (fis.read() != -1) { // each call = one syscall
                bytesRead1++;
            }
        }
        long elapsedUnbuffered = System.currentTimeMillis() - startUnbuffered;
        System.out.printf("  Bytes read : %,d%n", bytesRead1);
        System.out.printf("  Time       : %,d ms%n%n", elapsedUnbuffered);

        // --- Benchmark 2: Buffered (8 KB blocks) ---
        System.out.println("Benchmark 2: BufferedInputStream.read() — 8 KB buffer");
        int expectedSyscalls = (int) Math.ceil((double) FILE_SIZE / BUFFER_SIZE);
        System.out.printf("  Expected syscalls: ~%,d (one read(2) per %d-byte fill)%n",
                expectedSyscalls, BUFFER_SIZE);

        long startBuffered = System.currentTimeMillis();
        long bytesRead2 = 0;
        try (BufferedInputStream bis =
                     new BufferedInputStream(new FileInputStream(tempFile.toFile()), BUFFER_SIZE)) {
            while (bis.read() != -1) { // reads from in-memory buffer; refills with one syscall per 8 KB
                bytesRead2++;
            }
        }
        long elapsedBuffered = System.currentTimeMillis() - startBuffered;
        System.out.printf("  Bytes read : %,d%n", bytesRead2);
        System.out.printf("  Time       : %,d ms%n%n", elapsedBuffered);

        // --- Summary ---
        System.out.println("=== Summary ===");
        System.out.printf("  Unbuffered : %,6d ms   (~%,d syscalls)%n", elapsedUnbuffered, FILE_SIZE);
        System.out.printf("  Buffered   : %,6d ms   (~%,d syscalls)%n",  elapsedBuffered,   expectedSyscalls);

        if (elapsedUnbuffered > 0 && elapsedBuffered > 0) {
            double speedup = (double) elapsedUnbuffered / elapsedBuffered;
            System.out.printf("  Speedup    : %.1fx faster with buffering%n", speedup);
        }

        System.out.println();
        System.out.println("Why buffering matters:");
        System.out.println("  Each unbuffered read() crosses the user/kernel boundary (context switch).");
        System.out.println("  Cost: ~100–1000 ns per switch × 5,000,000 = seconds of pure overhead.");
        System.out.println("  BufferedInputStream batches 8,192 bytes per syscall — ~610 context switches.");
        System.out.println("  For NIO, use FileChannel + ByteBuffer or Files.readAllBytes() for bulk reads.");

        // Clean up
        Files.deleteIfExists(tempFile);
        System.out.println("\nTemp file deleted. Lab 5 complete.");
    }
}
