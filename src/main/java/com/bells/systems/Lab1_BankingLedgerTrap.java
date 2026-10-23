package com.bells.systems;

public class Lab1_BankingLedgerTrap {

    static class Account {
        private final String id;
        private double balance;

        public Account(String id, double balance) {
            this.id = id;
            this.balance = balance;
        }

        public double getBalance() { return balance; }
        public void setBalance(double balance) { this.balance = balance; }
        public String getId() { return id; }
    }

    // --- STEP 1 METHODS (THE TRAPS) ---
    // Trap A: Tries to mutate primitive parameter directly
    public static void applyFeeBroken(double bal, double fee) {
        bal -= fee; // Mutates local stack copy only!
    }

    // Trap B: Tries to reassign object reference parameter
    public static void upgradeAccountBroken(Account acc) {
        acc.setBalance(acc.getBalance() + 100.0); // Mutates object on heap (works)
        acc = new Account(acc.getId(), 999999.0); // Rebinds local stack pointer (ignored by caller)
    }

    // --- STEP 2 METHODS (THE CLEAN FIXES) ---
    // Fix A: Returns the updated value so caller updates its stack frame
    public static double applyFeeFixed(double bal, double fee) {
        return bal - fee;
    }

    // Fix B: Returns the new reference or mutates existing instance properly
    public static Account upgradeAccountFixed(Account acc) {
        acc.setBalance(acc.getBalance() + 100.0);
        return new Account(acc.getId(), 999999.0); // Caller explicitly captures returned reference
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" LAB 1: BANKING LEDGER (Pass-by-Value & Memory Realms)");
        System.out.println("======================================================================");

        double balance = 500.0;
        Account primaryAccount = new Account("SG-ACC-001", balance);

        // ----------------------------------------------------------------------------------
        // [STEP 1: THE FAILING TRAP - ACTIVE]
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> RUNNING STEP 1: The Broken Mutation & Reassignment Trap <<<");
        System.out.println("Initial Balance: $" + balance);
        
        applyFeeBroken(balance, 50.0);
        System.out.println("Balance after applyFeeBroken(): $" + balance + "  <-- HIT THE BLOCK: Still $500.0!");

        upgradeAccountBroken(primaryAccount);
        System.out.println("Account Balance after upgradeAccountBroken(): $" + primaryAccount.getBalance());
        System.out.println("  <-- HIT THE BLOCK: Mutating heap balance to $600 worked, but the $999,999");
        System.out.println("      new Account reassignment was lost because Java only copied the pointer!");

        // ----------------------------------------------------------------------------------
        // [STEP 2: THE FIX - UNCOMMENT TO DEMONSTRATE RESOLUTION]
        // ----------------------------------------------------------------------------------
        /*
        System.out.println("\n>>> RUNNING STEP 2: The Clean Fix (Return Re-assignment) <<<");
        
        balance = applyFeeFixed(balance, 50.0);
        System.out.println("Balance after applyFeeFixed(): $" + balance + "  <-- FIXED: Fee successfully deducted!");

        primaryAccount = upgradeAccountFixed(primaryAccount);
        System.out.println("Account Balance after upgradeAccountFixed(): $" + primaryAccount.getBalance() + 
                           "  <-- FIXED: Caller stack frame now points to the new upgraded Account!");
        */
    }
}