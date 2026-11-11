public class OrderItem {
    private String productName;
    private double price;
    private int quantity;

    public OrderItem(String productName, double price, int quantity) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return price * quantity; }

    @Override
    public String toString() {
        return String.format("%-22s x %-3d %10.0f VND", productName, quantity, getSubtotal());
    }
}
