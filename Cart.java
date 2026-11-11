import java.util.ArrayList;

public class Cart {
    private ArrayList<CartItem> items = new ArrayList<>();

    public ArrayList<CartItem> getItems() { return items; }
    public boolean isEmpty() { return items.isEmpty(); }

    public boolean addProduct(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product does not exist.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (quantity > product.getStock()) {
            throw new IllegalArgumentException("Insufficient stock.");
        }

        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                int newQuantity = item.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    throw new IllegalArgumentException(
                            "Total quantity in cart exceeds available stock.");
                }
                item.setQuantity(newQuantity);
                return true;
            }
        }

        items.add(new CartItem(product, quantity));
        return true;
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : items) total += item.getSubtotal();
        return total;
    }

    public void clear() { items.clear(); }
}
