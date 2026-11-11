import java.util.ArrayList;

public class Order {
    private String id;
    private String customerId;
    private ArrayList<OrderItem> items;
    private Payment payment;

    public Order(String id, String customerId, Cart cart, String paymentMethod) {
        this(id, customerId, cart, new Payment(paymentMethod, cart.getTotal()));
    }

    public Order(String id, String customerId, Cart cart, Payment payment) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            items.add(new OrderItem(item.getProduct().getName(), item.getProduct().getPrice(), item.getQuantity()));
        }
        this.payment = payment;
    }

    // Dùng khi nạp đơn hàng đã lưu từ file (không cần Cart).
    public Order(String id, String customerId, ArrayList<OrderItem> items, Payment payment) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.payment = payment;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public ArrayList<OrderItem> getItems() { return items; }
    public Payment getPayment() { return payment; }

    public void printDetails() {
        System.out.println("Order ID: " + id + " | Customer ID: " + customerId);
        for (OrderItem item : items) System.out.println("  " + item);
        System.out.printf("Total: %.0f VND%n", payment.getAmount());
        System.out.println("Payment method: " + payment.getMethod());
        System.out.println("Payment status: " + (payment.isPaid() ? "PAID" : "UNPAID"));
    }
}
