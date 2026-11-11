public class Product {
    private String id;
    private String name;
    private double price;
    private int stock;

    public Product(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public boolean reduceStock(int quantity) {
        if (quantity <= 0 || quantity > stock) return false;
        stock -= quantity;
        return true;
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-22s | %10.0f VND | Stock: %d", id, name, price, stock);
    }
}
