# Java Systems Fundamentals

A **Failure-First** teaching lab for a Java & System Fundamentals masterclass.  
Each lab is a self-contained program that deliberately triggers a real JVM or systems-level failure, explains the root cause, and points to the fix.

---

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java | 21 |
| Maven | 3.8+ |

Verify:
```bash
java  -version
mvn   -version
```

---

## Build

```bash
cd java-systems-fundamentals
mvn clean compile
```

---

## Labs

### Lab 1 — Stack Overflow (`Lab1_StackOverflow`)

Infinite recursion exhausts the JVM thread stack (`-Xss`).

```bash
# Default stack size
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab1_StackOverflow"

# Trigger faster with a tiny stack (256 KB)
java -Xss256k -cp target/classes com.bells.systems.Lab1_StackOverflow

# See more frames with a large stack
java -Xss8m   -cp target/classes com.bells.systems.Lab1_StackOverflow
```

**What you'll see:** `StackOverflowError` with the recursion depth reached.  
**Key concept:** Every call frame (locals + return address + operand stack) costs stack space. With no base case the JVM runs out and throws.  
**Fix:** Rewrite as an iterative loop — O(1) stack, no frames pile up.

---

### Lab 2 — Memory Leak / OutOfMemoryError (`Lab2_MemoryLeak`)

A static `List` retains 10 MB `byte[]` allocations, preventing GC.

```bash
# Cap the heap at 128 MB to see the OOM quickly
MAVEN_OPTS="-Xmx128m" mvn exec:java -Dexec.mainClass="com.bells.systems.Lab2_MemoryLeak"

# Or run directly
java -Xmx128m -cp target/classes com.bells.systems.Lab2_MemoryLeak

# Explore Metaspace limits (off-heap, class metadata)
java -Xmx256m -XX:MaxMetaspaceSize=32m -cp target/classes com.bells.systems.Lab2_MemoryLeak
```

**What you'll see:** Heap fills up chunk by chunk until `OutOfMemoryError: Java heap space`.  
**Key concept:** `static` fields are GC roots — anything they reference is **never** collected.  
**Fix:** Use a bounded `LinkedHashMap` with `removeEldestEntry`, `WeakHashMap`, or `SoftReference`.

---

### Lab 3 — CPU Cache Visibility (`Lab3_CpuCacheVisibility`)

A non-`volatile` flag is not seen by a worker thread due to L1/L2 cache isolation and JIT hoisting.

```bash
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab3_CpuCacheVisibility"
```

**What you'll see (usually):** Worker thread keeps spinning past the 2-second join deadline.  
**Key concept:** Modern CPUs keep per-core write buffers. Without `volatile`, the JIT may hoist the flag read out of the loop entirely (loop-invariant code motion). No memory barrier → no visibility guarantee.  
**Fix:** Declare `private static volatile boolean active = true;`

---

### Lab 4 — Race Condition vs. Atomic CAS (`Lab4_RaceCondition`)

Two threads race on `unsafeCount++` (read-modify-write is 3 bytecode ops) vs. `AtomicInteger.incrementAndGet()` (single CAS instruction).

```bash
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab4_RaceCondition"

# Disassemble to see the 3-step bytecode of counter++
javap -c target/classes/com/bells/systems/Lab4_RaceCondition.class
```

**What you'll see:** `unsafeCount` ends up less than 20,000; `AtomicInteger` is always exactly 20,000.  
**Key concept:** `counter++` compiles to `getfield → iadd → putfield`. Two threads can interleave between any step, silently dropping an increment.  
**Fix:** Use `AtomicInteger`, `LongAdder`, or `synchronized` — all guarantee atomicity.

---

### Lab 5 — Syscall Benchmark (`Lab5_SyscallBenchmark`)

Measures the cost of 5,000,000 kernel context switches (unbuffered) vs. ~610 (buffered 8 KB reads).

```bash
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab5_SyscallBenchmark"
```

**What you'll see:** Unbuffered reads take orders of magnitude longer than buffered reads.  
**Key concept:** Every `FileInputStream.read()` for a single byte issues one `read(2)` syscall — a user→kernel→user mode switch costing ~100–1000 ns each. `BufferedInputStream` fills an 8 KB buffer per syscall, reducing them by ~8,000×.  
**Fix:** Always wrap file I/O with `BufferedInputStream`/`BufferedOutputStream`, or use `Files.readAllBytes()` / `FileChannel` + `ByteBuffer` for bulk operations.

---

### Lab 6 — Deadlock Diagnostics (`Lab6_DeadlockDiagnostics`)

Two threads acquire two locks in inverted order, producing a circular wait that hangs forever.

```bash
# Step 1: start the program (it will HANG)
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab6_DeadlockDiagnostics"

# Step 2: note the PID printed to the console, then in a second terminal:
jstack <PID>                    # full thread dump + deadlock analysis
jcmd   <PID> Thread.print       # alternative
kill -3 <PID>                   # SIGQUIT → JVM prints thread dump to stdout

# Step 3: look for this section in the jstack output:
#   Found one Java-level deadlock:
#   "Thread-2": waiting to lock monitor ...
#   "Thread-1": waiting to lock monitor ...
```

**What you'll see:** Program hangs. `jstack` reports `"Found one Java-level deadlock:"`.  
**Key concept:** Circular lock acquisition satisfies Coffman's four conditions (mutual exclusion, hold-and-wait, no preemption, circular wait) → permanent deadlock.  
**Fix:** Always acquire locks in a **consistent global order** (e.g. sort by `System.identityHashCode()`), or use `Lock.tryLock(timeout)` to detect contention and back off.

---

## Quick Reference — All Commands

```bash
# Compile
mvn clean compile

# Run each lab
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab1_StackOverflow"
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab2_MemoryLeak"
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab3_CpuCacheVisibility"
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab4_RaceCondition"
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab5_SyscallBenchmark"
mvn exec:java -Dexec.mainClass="com.bells.systems.Lab6_DeadlockDiagnostics"

# Labs with JVM flags
java -Xss256k  -cp target/classes com.bells.systems.Lab1_StackOverflow
java -Xmx128m  -cp target/classes com.bells.systems.Lab2_MemoryLeak

# Bytecode disassembly (Lab 4)
javap -c target/classes/com/bells/systems/Lab4_RaceCondition.class
```
