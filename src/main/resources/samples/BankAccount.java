package samples;

/**
 * Sample domain class representing a Bank Account.
 * Demonstrates business invariants, validation boundaries, and exception paths.
 */
public class BankAccount {
    private final String accountNumber;
    private String ownerName;
    private double balance;
    private boolean locked;

    public BankAccount(String accountNumber, String ownerName, double initialBalance) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be null or empty");
        }
        if (initialBalance < 0.0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.locked = false;
    }

    public double deposit(double amount) {
        if (locked) {
            throw new IllegalStateException("Cannot deposit into a locked account");
        }
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance += amount;
        return this.balance;
    }

    public double withdraw(double amount) {
        if (locked) {
            throw new IllegalStateException("Cannot withdraw from a locked account");
        }
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount > this.balance) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal");
        }
        this.balance -= amount;
        return this.balance;
    }

    public boolean transfer(BankAccount targetAccount, double amount) {
        if (targetAccount == null) {
            throw new IllegalArgumentException("Target account cannot be null");
        }
        if (this.locked || targetAccount.isLocked()) {
            throw new IllegalStateException("Both accounts must be unlocked for transfer");
        }
        if (amount <= 0.0 || amount > this.balance) {
            return false;
        }
        this.withdraw(amount);
        targetAccount.deposit(amount);
        return true;
    }

    public double calculateInterest(double annualRatePercentage, int years) {
        if (annualRatePercentage < 0.0 || years < 0) {
            throw new IllegalArgumentException("Interest rate and years must be non-negative");
        }
        if (years == 0 || annualRatePercentage == 0.0) {
            return 0.0;
        }
        return this.balance * (annualRatePercentage / 100.0) * years;
    }

    public void lockAccount() {
        this.locked = true;
    }

    public void unlockAccount() {
        this.locked = false;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty");
        }
        this.ownerName = ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isLocked() {
        return locked;
    }
}
