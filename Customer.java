import java.util.ArrayList;

public class Customer extends User {
    private Cart cart;
    private ArrayList<Order> orderHistory;

    public Customer(String id, String username, String password) {
        super(id, username, password);
        cart = new Cart();
        orderHistory = new ArrayList<>();
    }

    public Cart getCart() {
        return cart;
    }

    public ArrayList<Order> getOrderHistory() {
        return orderHistory;
    }

    public void addOrder(Order order) {
        orderHistory.add(order);
    }

    @Override
    public String getRole() {
        return "Customer";
    }
}
