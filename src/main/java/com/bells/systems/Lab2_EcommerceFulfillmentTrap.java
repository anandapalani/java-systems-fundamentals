package com.bells.systems;

import java.util.ArrayList;
import java.util.List;

public class Lab2_EcommerceFulfillmentTrap {

    // --- DOMAIN MODEL & CONTRACTS ---
    public interface Payable {
        double calculateFinalPrice();
        String getItemName();
    }

    public static class PhysicalItem implements Payable {
        private final String title;
        private final double price;

        public PhysicalItem(String title, double price) {
            this.title = title;
            this.price = price;
        }

        @Override
        public double calculateFinalPrice() { return price + 5.0; } // price + shipping

        @Override
        public String getItemName() { return "Physical Book: " + title; }
    }

    public static class DigitalSubscription implements Payable {
        private final String planName;
        private final double fee;

        public DigitalSubscription(String planName, double fee) {
            this.planName = planName;
            this.fee = fee;
        }

        @Override
        public double calculateFinalPrice() { return fee; }

        @Override
        public String getItemName() { return "Digital Sub: " + planName; }
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" LAB 2: E-COMMERCE ENGINE (Polymorphism & Interface Contracts)");
        System.out.println("======================================================================");

        // ----------------------------------------------------------------------------------
        // [STEP 1: THE FAILING TRAP - ACTIVE]
        // Demonstrates the fragility of raw collections and manual casting
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> RUNNING STEP 1: The Raw Collection Crash <<<");
        ArrayList rawCart = new ArrayList();
        rawCart.add(new PhysicalItem("Clean Code", 45.0));
        rawCart.add(new DigitalSubscription("Cloud Access", 19.99));

        System.out.println("Processing raw collection with manual casting (PhysicalItem)...");
        try {
            for (Object obj : rawCart) {
                // Brittle assumption: forces every object into PhysicalItem
                PhysicalItem item = (PhysicalItem) obj;
                System.out.println("  Processed: " + item.getItemName());
            }
        } catch (ClassCastException ex) {
            System.err.println("\n💥 CRASH DETECTED: java.lang.ClassCastException!");
            System.err.println("   Message: " + ex.getMessage());
            System.out.println("   --> BLOCK ENCOUNTERED: Raw collections defer type safety bugs to runtime!");
            System.out.println("   --> ACTION: Comment Step 1 and uncomment Step 2 below.");
        }

        // ----------------------------------------------------------------------------------
        // [STEP 2: THE FIX - UNCOMMENT TO DEMONSTRATE RESOLUTION]
        // Demonstrates interface contracts and dynamic dispatch
        // ----------------------------------------------------------------------------------
        /*
        System.out.println("\n>>> RUNNING STEP 2: Interface Contract (List<Payable>) <<<");
        List<Payable> safeCart = new ArrayList<>();
        safeCart.add(new PhysicalItem("Clean Code", 45.0));
        safeCart.add(new DigitalSubscription("Cloud Access", 19.99));

        double grandTotal = 0.0;
        for (Payable item : safeCart) {
            System.out.printf("  Processed safely: %-30s | Price: $%.2f%n", 
                              item.getItemName(), item.calculateFinalPrice());
            grandTotal += item.calculateFinalPrice();
        }
        System.out.printf("%nGrand Total via Dynamic Dispatch: $%.2f (Zero casts, 100%% type safe)%n", grandTotal);
        */
    }
}