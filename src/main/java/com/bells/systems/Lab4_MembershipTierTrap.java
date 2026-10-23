package com.bells.systems;

public class Lab4_MembershipTierTrap {

    // --- DOMAIN MODEL (BASE CLASS) ---
    public abstract static class Member {
        protected String memberId;
        protected String name;
        protected int points;

        // Custom parameterized constructor: Java will NOT provide a default no-arg Member()!
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

    // ==================================================================================
    // [STEP 1: THE BROKEN SUBCLASS (TRAP) - ACTIVE]
    // ==================================================================================
    public static class NormalMemberBroken extends Member {

        // 🔴 TRAP 1: MISSING super(memberId, name)
        // If a student writes:
        // public NormalMemberBroken(String memberId, String name) {
        //     this.memberId = memberId;
        //     this.name = name;
        // }
        // javac automatically inserts super() with NO arguments, which does not exist in Member!
        // It produces: "error: constructor Member in class Member cannot be applied to given types"
        
        public NormalMemberBroken(String memberId, String name) {
            // Uncommenting the line below and commenting super(...) triggers the compile-time block:
            // /* javac error */ // (no super call)
            super(memberId, name); // Fixed here so the class compiles for Step 1 execution
        }

        // 🔴 TRAP 2: TYPO WITHOUT @Override
        // Student writes 'calculateDiscouunt' with double 'u' and omits @Override.
        // The compiler treats this as a brand-new unrelated method!
        public double calculateDiscouunt(double billAmount) { 
            return billAmount * 0.05; // Expected 5% discount
        }

        // To satisfy the abstract method contract, they put a dummy return or stub:
        @Override
        public double calculateDiscount(double billAmount) {
            return 0.0; // The actual business logic above was bypassed!
        }
    }

    // ==================================================================================
    // [STEP 2: THE FIXED SUBCLASS - UNCOMMENT TO DEMONSTRATE RESOLUTION]
    // ==================================================================================
    /*
    public static class VIPMemberFixed extends Member {
        private final double discountRate = 0.20; // 20% discount

        // 🟢 FIX 1: Proper Constructor Chaining via super()
        public VIPMemberFixed(String memberId, String name) {
            super(memberId, name); // Explicitly initializes shared parent state on the Heap
        }

        // 🟢 FIX 2: Strict @Override Annotation
        // If you make a typo with @Override present, javac fails immediately:
        // "error: method does not override or implement a method from a supertype"
        @Override
        public double calculateDiscount(double billAmount) {
            this.points += (int)(billAmount * 0.1); // Accumulate reward points
            return billAmount * discountRate;
        }
    }
    */

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" LAB 4: MEMBERSHIP TIER (Inheritance, super(), & @Override Traps)");
        System.out.println("======================================================================");

        double cartTotal = 200.0;

        // ----------------------------------------------------------------------------------
        // [STEP 1 EXECUTION]
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> RUNNING STEP 1: The Broken Override Trap <<<");
        Member memberBroken = new NormalMemberBroken("MBR-001", "Tan Ah Kow");
        System.out.println("Customer Profile: " + memberBroken.getProfile());

        double discountBroken = memberBroken.calculateDiscount(cartTotal);
        System.out.println("Cart Total: $" + cartTotal);
        System.out.println("Discount Applied: $" + discountBroken + "  <-- HIT THE BLOCK: $0.00 discount!");
        System.out.println("Reason: Typo in 'calculateDiscouunt' without @Override was completely ignored by runtime dispatch.");

        // ----------------------------------------------------------------------------------
        // [STEP 2 EXECUTION - UNCOMMENT BELOW ALONG WITH VIPMemberFixed]
        // ----------------------------------------------------------------------------------
        /*
        System.out.println("\n>>> RUNNING STEP 2: Strict @Override & super() Chaining <<<");
        Member memberFixed = new VIPMemberFixed("VIP-999", "Tan Ah Kow");
        System.out.println("Customer Profile: " + memberFixed.getProfile());

        double discountFixed = memberFixed.calculateDiscount(cartTotal);
        System.out.println("Cart Total: $" + cartTotal);
        System.out.println("Discount Applied: $" + discountFixed + "  <-- FIXED: $40.00 (20% VIP discount applied)");
        System.out.println("Updated Profile: " + memberFixed.getProfile() + " (Points updated automatically)");
        */
    }
}