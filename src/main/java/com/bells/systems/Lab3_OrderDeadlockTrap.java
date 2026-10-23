package com.bells.systems;

public class Lab3_OrderDeadlockTrap {

    private static final Object INVENTORY_LOCK = new Object();
    private static final Object PAYMENT_LOCK = new Object();

    public static void main(String[] args) {
        long pid = ProcessHandle.current().pid();
        System.out.println("======================================================================");
        System.out.println(" LAB 3: PRODUCTION DEADLOCK (Live Terminal Diagnostics)");
        System.out.println("======================================================================");
        System.out.println("Process PID: " + pid);

        // ----------------------------------------------------------------------------------
        // [STEP 1: THE SILENT DEADLOCK - ACTIVE]
        // Thread 1: Lock Inventory -> Lock Payment
        // Thread 2: Lock Payment   -> Lock Inventory (Inverted order triggers circular wait)
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> RUNNING STEP 1: Inverted Lock Ordering (Deadlock Simulation) <<<");
        System.out.println("Notice: If this hangs, open a 2nd terminal and run: jstack " + pid);

        Thread tx1Broken = new Thread(() -> {
            synchronized (INVENTORY_LOCK) {
                System.out.println("[Tx-1] Reserved Inventory. Waiting for Payment Lock...");
                pause(200);
                synchronized (PAYMENT_LOCK) {
                    System.out.println("[Tx-1] Order completed successfully.");
                }
            }
        }, "Order-Tx-1");

        Thread tx2Broken = new Thread(() -> {
            synchronized (PAYMENT_LOCK) {
                System.out.println("[Tx-2] Pre-authorized Payment. Waiting for Inventory Lock...");
                pause(200);
                synchronized (INVENTORY_LOCK) {
                    System.out.println("[Tx-2] Order completed successfully.");
                }
            }
        }, "Order-Tx-2");

        tx1Broken.start();
        tx2Broken.start();

        // ----------------------------------------------------------------------------------
        // [STEP 2: THE FIX - UNCOMMENT TO DEMONSTRATE RESOLUTION]
        // Enforce Global Lock Ordering: BOTH threads must acquire INVENTORY_LOCK before PAYMENT_LOCK
        // ----------------------------------------------------------------------------------
        /*
        System.out.println("\n>>> RUNNING STEP 2: Enforced Global Lock Ordering (No Deadlock) <<<");

        Thread tx1Fixed = new Thread(() -> {
            synchronized (INVENTORY_LOCK) {
                System.out.println("[Tx-1] Acquired Inventory Lock (Step 1)");
                pause(100);
                synchronized (PAYMENT_LOCK) {
                    System.out.println("[Tx-1] Acquired Payment Lock (Step 2) -> Order completed!");
                }
            }
        }, "Order-Tx-1");

        Thread tx2Fixed = new Thread(() -> {
            synchronized (INVENTORY_LOCK) { // Same lock order as Tx-1
                System.out.println("[Tx-2] Acquired Inventory Lock (Step 1)");
                pause(100);
                synchronized (PAYMENT_LOCK) {
                    System.out.println("[Tx-2] Acquired Payment Lock (Step 2) -> Order completed!");
                }
            }
        }, "Order-Tx-2");

        tx1Fixed.start();
        tx2Fixed.start();
        */
    }

    private static void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}