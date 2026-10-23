package com.bells.systems;

public class Lab4_MembershipTierTrap {

    // --- DOMAIN MODEL ---
    public abstract static class Member {
        protected String memberId;
        protected String name;
        protected int points;

        // Parent constructor initializing shared DNA/State
        public Member(String memberId, String name) {
            this.memberId = memberId;
            this.name = name;
            this.points = 0;
        }

        // Abstract method: Enforces concrete calculation on every tier
        public abstract double calculateDiscount(double billAmount);

        public String getProfile() {
            return "Member ID: " + memberId + " | Name: " + name + " | Points: " + points;
        }
    }

    // --- STEP 1: THE BROKEN SUBCLASS (TRAP) ---
    // If you don't call super() or provide matching constructor signatures,
    // or if a typo occurs in an overridden method without @Override:
    public static class NormalMemberBroken extends Member {
        // Trap: Attempting to bypass parent constructor leads to uninitialized state or compile errors
        public NormalMemberBroken(String memberId, String name) {
            super(memberId, name);
        }

        // TRAP: Typo in method name without @Override!
        // Compiles as a BRAND NEW method. The polymorphism dispatch falls back or fails silently!
        public double calculateDiscouunt(double billAmount) { 
            return billAmount * 0.05; // 5% discount
        }

        // To make it compile despite missing the abstract method:
        @Override
        public double calculateDiscount(double billAmount) {
            return 0.0; // Forgot real business logic!
        }
    }

    // --- STEP 2: THE FIXED SUBCLASS ---
    public static class VIPMemberFixed extends Member {
        private final double discountRate = 0.20; // 20% discount

        public VIPMemberFixed(String memberId, String name) {
            super(memberId, name); // Properly initializes inherited state
        }

        @Override // Compile-time safety net guarantees exact signature match
        public double calculateDiscount(double billAmount) {
            this.points += (int)(billAmount * 0.1); // Accumulate reward points
            return billAmount * discountRate;
        }
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" LAB 4: MEMBERSHIP TIER (Inheritance, super(), & @Override Traps)");
        System.out.println("======================================================================");

        double cartTotal = 200.0;

        // ----------------------------------------------------------------------------------
        // [STEP 1: THE BROKEN SUBCLASS - ACTIVE]
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> RUNNING STEP 1: The Broken Override Trap <<<");
        Member memberBroken = new NormalMemberBroken("MBR-001", "Tan Ah Kow");
        System.out.println("Customer Profile: " + memberBroken.getProfile());

        double discountBroken = memberBroken.calculateDiscount(cartTotal);
        System.out.println("Cart Total: $" + cartTotal);
        System.out.println("Discount Applied: $" + discountBroken + "  <-- HIT THE BLOCK: $0.00 discount!");
        System.out.println("Reason: Typo in 'calculateDiscouunt' without compiler checking caused the method to be ignored.");

        // ----------------------------------------------------------------------------------
        // [STEP 2: THE CLEAN FIX - UNCOMMENT TO DEMONSTRATE RESOLUTION]
        // ----------------------------------------------------------------------------------
        /*
        System.out.println("\n>>> RUNNING STEP 2: Strict @Override & State Chaining <<<");
        Member memberFixed = new VIPMemberFixed("VIP-999", "Tan Ah Kow");
        System.out.println("Customer Profile: " + memberFixed.getProfile());

        double discountFixed = memberFixed.calculateDiscount(cartTotal);
        System.out.println("Cart Total: $" + cartTotal);
        System.out.println("Discount Applied: $" + discountFixed + "  <-- FIXED: $40.00 (20% VIP discount applied)");
        System.out.println("Updated Profile: " + memberFixed.getProfile() + " (Points updated automatically)");
        */
    }
}