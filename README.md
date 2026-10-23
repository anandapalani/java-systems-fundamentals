# Java Systems Fundamentals — Failure-First Masterclass

Enterprise-grade lab suite for the **BELLS SCTP Software Engineering** cohort.  
Each lab triggers a real compile-time or runtime failure in a meaningful business domain, explains the JVM mechanics, and provides a clean production fix.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.8+ |
| Python | 3.9+ (for slide generation) |

```bash
java -version && mvn -version
```

---

## Build

```bash
cd java-systems-fundamentals
mvn clean compile
```

---

## Labs

### Lab 1 — Banking Ledger Trap (`BankingLedgerTrap`)
**LO2 & LO3 — Pass-by-Value & JVM Memory Realms**

**Trap 1 — Primitive double mutation:**  
`applyFee(balance, fee)` subtracts from a local copy. The caller's `ledgerBalance` stays unchanged. The fee deduction is silently lost.

**Trap 2 — Object pointer re-assignment:**  
`upgradeAccount(acc)` can mutate the Heap object via `setBalance()` (visible to caller), but `acc = new Account(...)` only rebinds the local Stack pointer — the caller's variable still points to the original.

```bash
mvn exec:java -Dexec.mainClass="com.bells.systems.BankingLedgerTrap"
```

---

### Lab 2 — E-Commerce Fulfillment Trap (`EcommerceFulfillmentTrap`)
**LO4 & LO5 — OOP, Polymorphism, Abstract Classes vs Interfaces**

**Trap:** Storing `PhysicalItem` and `DigitalSubscription` in a raw `ArrayList` compiles but throws `ClassCastException` at runtime when the first digital item is cast to `PhysicalItem`.

**Fix:** `List<Payable>` accepts any `Payable` implementor. Polymorphic dispatch calls the correct `calculateFinalPrice()` on each item — no `instanceof`, no casts, no `ClassCastException`.

**Bonus:** `abstract class Customer` enforces `applyDiscount()` at compile time. `super(customerId, name)` in `VIPCustomer` initialises inherited Heap fields correctly.

```bash
mvn exec:java -Dexec.mainClass="com.bells.systems.EcommerceFulfillmentTrap"
```

---

### Lab 3 — Order Fulfillment Deadlock (`OrderFulfillmentDeadlock`)
**Production Diagnostics — Circular lock ordering & jstack**

**Trap:** `Order-Tx-1` acquires `INVENTORY_LOCK` then waits for `PAYMENT_LOCK`. `Order-Tx-2` acquires `PAYMENT_LOCK` then waits for `INVENTORY_LOCK`. Both threads block forever. Service shows 0% CPU. No exception. No log output.

**Diagnosis steps:**

```bash
# Step 1: start the lab (it WILL HANG — that's the point)
mvn exec:java -Dexec.mainClass="com.bells.systems.OrderFulfillmentDeadlock"

# Step 2: in a second terminal
jps                    # find the PID if you missed it
jstack <PID>           # full thread dump + deadlock analysis

# Step 3: search for this in the output:
#   Found one Java-level deadlock:
```

**Fix:** Always acquire `INVENTORY_LOCK` before `PAYMENT_LOCK` in both threads — consistent lock ordering breaks the circular wait condition.

---

## Quick Reference

```bash
# Build everything
mvn clean compile

# Run labs
mvn exec:java -Dexec.mainClass="com.bells.systems.BankingLedgerTrap"
mvn exec:java -Dexec.mainClass="com.bells.systems.EcommerceFulfillmentTrap"
mvn exec:java -Dexec.mainClass="com.bells.systems.OrderFulfillmentDeadlock"
```

---

## Curriculum Mapping

| Lab | Class | LO | Failure Domain |
|-----|-------|----|----------------|
| 1 | `BankingLedgerTrap` | LO2, LO3 | Primitive copy vs. heap mutation, pointer re-assignment |
| 2 | `EcommerceFulfillmentTrap` | LO4, LO5 | ClassCastException, abstract contract, interface dispatch |
| 3 | `OrderFulfillmentDeadlock` | Production | Circular lock ordering, jstack diagnostics |

---

## Generate the Slide Deck

```bash
# From the project root:
python3 make_deck.py
# → Java_Masterclass_Deck.pptx  (14 slides)
```
