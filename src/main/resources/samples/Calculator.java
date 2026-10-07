package samples;

/**
 * Sample math calculator demonstrating boundary values, equivalence partitioning,
 * loops, conditional branches, and arithmetic operators for mutation analysis.
 */
public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public double divide(double numerator, double denominator) {
        if (denominator == 0.0) {
            throw new ArithmeticException("Division by zero is not allowed");
        }
        return numerator / denominator;
    }

    public long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial is undefined for negative numbers");
        }
        if (n > 20) {
            throw new IllegalArgumentException("Factorial input too large (causes 64-bit overflow)");
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    public boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        if (n == 2 || n == 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }
        if (n > 1000000) {
            throw new IllegalArgumentException("Input too large for prime test");
        }
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }

    public double power(double base, int exponent) {
        if (exponent < 0) {
            if (base == 0.0) {
                throw new ArithmeticException("Zero cannot be raised to a negative power");
            }
            if (exponent < -50) {
                throw new IllegalArgumentException("Negative exponent too large");
            }
            return 1.0 / power(base, -exponent);
        }
        if (exponent > 50) {
            throw new IllegalArgumentException("Exponent too large");
        }
        double result = 1.0;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }

    public int clamp(int value, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min cannot be greater than max");
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
