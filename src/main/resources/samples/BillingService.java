package samples;

/**
 * Sample demonstrating linked functions and sub-function fault isolation:
 * The caller function 'processBilling' calls the sub-function 'calculateInstallment'.
 * When 'installmentsCount' is 0, the sub-function crashes with an ArithmeticException,
 * allowing the Test Suite Matrix to identify the exact sub-function that caused the failure.
 */
public class BillingService {

    // Function 1: Caller function (Main entry point tested)
    public int processBilling(int totalInvoiceAmount, int installmentsCount) {
        if (totalInvoiceAmount < 0) {
            throw new IllegalArgumentException("Invoice amount cannot be negative");
        }

        // Linked call to sub-function
        int baseInstallment = calculateInstallment(totalInvoiceAmount, installmentsCount);

        int processingFee = 15;
        return baseInstallment + processingFee;
    }

    // Function 2: Callee / Sub-function (Where the defect occurs)
    public int calculateInstallment(int amount, int count) {
        // Missing guard check: when count is 0, integer division by zero throws ArithmeticException
        return amount / count;
    }
}
