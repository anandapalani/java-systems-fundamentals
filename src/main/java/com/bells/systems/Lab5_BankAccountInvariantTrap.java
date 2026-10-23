package com.bells.systems;

public class Lab5_BankAccountInvariantTrap {

    // --- STEP 1: BROKEN CLASS (Public variables - No Encapsulation) ---
    public static class BankAccountBroken {
        public String accountNumber;
        public double balance; // DANGEROUS: Any class can mutate this directly!

        public BankAccountBroken(String accountNumber, double balance) {
            this.accountNumber = accountNumber;
            this.balance = balance;
        }
    }

    // --- STEP 2: FIXED CLASS (Encapsulated Invariant Guard) ---
    public static class BankAccountFixed {
        private final String accountNumber;
        private double balance; // Protected from external tampering

        public BankAccountFixed(String accountNumber, double balance) {
            if (balance < 0) {
                throw new IllegalArgumentException("Initial balance cannot be negative.");
            }
            this.accountNumber = accountNumber;
            this.balance = balance;
        }

        public double getBalance() { return balance; }
        public String getAccountNumber() { return accountNumber; }

        public void withdraw(double amount) {
            if (amount <= 0) {
                System.out.println("Withdrawal rejected: Amount must be positive.");
                return;
            }
            if (amount > balance) {
                System.out.println("Withdrawal rejected: Insufficient funds ($" + balance + " available).");
                return;
            }
            balance -= amount;
            System.out.println("Successfully withdrew $" + amount + ". New balance: $" + balance);
        }
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" LAB 5: BANK ACCOUNT INVARIANT (Encapsulation & UML Modeling)");
        System.out.println("======================================================================");

        // ----------------------------------------------------------------------------------
        // [STEP 1: THE BROKEN MUTATION - ACTIVE]
        // Direct public field tampering corrupts data state
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> RUNNING STEP 1: Data State Corruption (Public Fields) <<<");
        BankAccountBroken accountBroken = new BankAccountBroken("SG-CORRUPT-01", 1000.0);
        System.out.println("Initial Balance: $" + accountBroken.balance);

        // A malicious or buggy rogue service bypasses checks and sets negative balance
        accountBroken.balance = -50000.0;
        System.out.println("Balance after rogue mutation: $" + accountBroken.balance);
        System.out.println("--> BLOCK ENCOUNTERED: Business rule violated! Balance is negative.");
        System.out.println("--> UML IMPACT: Represented as '+ balance: double' (Violates OOP best practices).");

        // ----------------------------------------------------------------------------------
        // [STEP 2: THE ENCAPSULATED FIX - UNCOMMENT TO DEMONSTRATE RESOLUTION]
        // ----------------------------------------------------------------------------------
        /*
        System.out.println("\n>>> RUNNING STEP 2: Encapsulated State Protection <<<");
        BankAccountFixed accountFixed = new BankAccountFixed("SG-SECURE-01", 1000.0);
        System.out.println("Initial Balance: $" + accountFixed.getBalance());

        // Rogue attempt to overdraw is blocked by method logic
        accountFixed.withdraw(1500.0);
        System.out.println("Balance remains intact: $" + accountFixed.getBalance());

        accountFixed.withdraw(300.0);
        System.out.println("--> UML IMPACT: Represented as '- balance: double' with '+ withdraw(double): void'.");
        */
    }
}