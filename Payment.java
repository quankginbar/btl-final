public class Payment implements Payable {
    private String method;
    private double amount;
    private boolean paid;

    public Payment(String method, double amount) {
        this.method = method;
        this.amount = amount;
        this.paid = false;
    }

    // Choice 0 = Cash on delivery; choice 1 = Bank transfer (demo).
    public static Payment create(int choice, double amount) {
        switch (choice) {
            case 0:
                return new Payment("Cash on delivery", amount);
            case 1:
                Payment payment = new Payment("Bank transfer (demo)", amount);
                payment.processPayment();
                return payment;
            default:
                throw new IllegalArgumentException("Invalid payment method.");
        }
    }

    @Override
    public void processPayment() {
        paid = true;
    }

    public String getMethod() { return method; }
    public double getAmount() { return amount; }

    @Override
    public boolean isPaid() { return paid; }
}
