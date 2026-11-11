import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

public class ShoppingSwingApp extends JFrame {
    // ==================== PHẦN DÙNG CHUNG: DỮ LIỆU VÀ ĐIỀU HƯỚNG ====================
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Product> products = new ArrayList<>();
    private int nextCustomerId = 2;
    private int nextProductId = 4;
    private final CheckoutService checkoutService = new CheckoutService();
    private final DataStorage storage = new DataStorage("data"); // thư mục data/ cạnh ứng dụng
    private User currentUser;

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    // Các bảng và vùng hiển thị được sử dụng bởi nhiều chức năng.
    private DefaultTableModel productModel = new DefaultTableModel(new Object[]{"ID", "Name", "Price", "Stock"}, 0);
    private DefaultTableModel adminProductModel = new DefaultTableModel(new Object[]{"ID", "Name", "Price", "Stock"}, 0);
    private JTextArea cartArea = new JTextArea(10, 30);
    private JTextArea orderHistoryArea = new JTextArea(10, 30);

    //  KHỞI TẠO ỨNG DỤNG 
    public ShoppingSwingApp() {
        // Nạp dữ liệu đã lưu; lần chạy đầu tiên thì tạo dữ liệu mẫu và lưu ra file.
        loadOrSeedData();

        // Cấu hình cửa sổ chính.
        setTitle("Shopping Online System");
        setSize(700, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveData();
            }
        });
        setLocationRelativeTo(null);

        // Đăng ký các màn hình vào CardLayout.
        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createCustomerPanel(), "CUSTOMER");
        mainPanel.add(createAdminPanel(), "ADMIN");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }


    // LƯU / NẠP DỮ LIỆU BẰNG FILE TXT
    // Lớp xử lý file: DataStorage.java.
    private void seedSampleData() {
        users.clear();
        products.clear();
        users.add(new Admin("A001", "admin", "admin123"));
        users.add(new Customer("C001", "quan", "123456"));
        products.add(new Product("P001", "Keyboard", 350000, 10));
        products.add(new Product("P002", "Mouse", 150000, 15));
        products.add(new Product("P003", "Headphones", 450000, 8));
    }

    private void loadOrSeedData() {
        if (storage.exists()) {
            try {
                storage.load(users, products);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not read saved data, using sample data: " + ex.getMessage());
                seedSampleData();
            }
        } else {
            seedSampleData();
            saveData();
        }
        updateNextIds();
    }

    // Gọi sau mỗi thay đổi dữ liệu (đăng ký, thêm sản phẩm, giỏ hàng, thanh toán, đổi mật khẩu).
    private void saveData() {
        try {
            storage.save(users, products);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
        }
    }

    // Tính lại bộ đếm ID từ dữ liệu đã nạp để ID mới không trùng ID cũ.
    private void updateNextIds() {
        int maxCustomer = 0, maxProduct = 0, maxOrder = 0;
        for (User u : users) {
            if (!(u instanceof Customer)) continue;
            maxCustomer = Math.max(maxCustomer, idNumber(u.getId()));
            for (Order o : ((Customer) u).getOrderHistory()) {
                maxOrder = Math.max(maxOrder, idNumber(o.getId()));
            }
        }
        for (Product p : products) maxProduct = Math.max(maxProduct, idNumber(p.getId()));

        nextCustomerId = maxCustomer + 1;
        nextProductId = maxProduct + 1;
        checkoutService.setNextOrderId(maxOrder + 1);
    }

    // "C012" -> 12; trả về 0 nếu ID không đúng định dạng.
    private static int idNumber(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    // TÀI KHOẢN: ĐĂNG NHẬP, ĐĂNG KÝ VÀ ĐỔI MẬT KHẨU
    // Các lớp nghiệp vụ liên quan: User.java, Customer.java, Admin.java.
   
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(80, 150, 80, 150));

        JTextField userField = new JTextField("quan");
        JPasswordField passField = new JPasswordField("123456");
        JButton loginBtn = new JButton("1. Login");
        JButton registerBtn = new JButton("2. Register Customer");

        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(registerBtn);
        panel.add(new JLabel("Demo Admin: admin / admin123"));
        panel.add(new JLabel("Demo Customer: quan / 123456"));

        // Đăng nhập và chuyển người dùng đến màn hình theo vai trò.
        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            for (User user : users) {
                if (user.login(u, p)) {
                    currentUser = user;
                    refreshData();
                    if (user instanceof Admin) cardLayout.show(mainPanel, "ADMIN");
                    else cardLayout.show(mainPanel, "CUSTOMER");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Incorrect username or password.");
        });

        // Đăng ký khách hàng mới và ngăn tên đăng nhập bị trùng.
        registerBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            if (u.isEmpty() || p.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.");
                return;
            }
            for (User user : users) {
                if (user.getUsername().equalsIgnoreCase(u)) {
                    JOptionPane.showMessageDialog(this, "Username already exists.");
                    return;
                }
            }
            String id = String.format("C%03d", nextCustomerId++);
            users.add(new Customer(id, u, p));
            saveData();
            JOptionPane.showMessageDialog(this, "Registered! Customer ID: " + id);
        });

        return panel;
    }

    private void changePasswordDialog() {
        String oldP = JOptionPane.showInputDialog(this, "Current password:");
        String newP = JOptionPane.showInputDialog(this, "New password:");
        if (oldP != null && newP != null && currentUser.changePassword(oldP, newP)) {
            saveData();
            JOptionPane.showMessageDialog(this, "Password changed successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Password change failed!");
        }
    }

    
    // SẢN PHẨM VÀ CHỨC NĂNG QUẢN TRỊ VIÊN
    // Lớp nghiệp vụ liên quan: Product.java.
    
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(new JTable(adminProductModel)), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout());
        JTextField nameField = new JTextField(10);
        JTextField priceField = new JTextField(7);
        JTextField stockField = new JTextField(4);
        JButton addBtn = new JButton("Add Product");
        JButton logoutBtn = new JButton("Logout");

        bottom.add(new JLabel("Name:")); bottom.add(nameField);
        bottom.add(new JLabel("Price:")); bottom.add(priceField);
        bottom.add(new JLabel("Stock:")); bottom.add(stockField);
        bottom.add(addBtn);
        bottom.add(logoutBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        // Kiểm tra dữ liệu rồi thêm sản phẩm vào danh sách.
        addBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                if (name.isEmpty() || price <= 0 || stock < 0) {
                    JOptionPane.showMessageDialog(this, "Invalid product data!");
                    return;
                }
                String id = String.format("P%03d", nextProductId++);
                products.add(new Product(id, name, price, stock));
                saveData();
                refreshData();
                JOptionPane.showMessageDialog(this, "Product added: " + id);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and stock.");
            }
        });

        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        return panel;
    }

    // KHÁCH HÀNG: XEM SẢN PHẨM VÀ QUẢN LÝ GIỎ HÀNG
    // Các lớp nghiệp vụ liên quan: Cart.java, CartItem.java, Product.java.
    
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(productModel);

        // Các nút chức năng dành cho khách hàng.
        JPanel bottom = new JPanel(new FlowLayout());
        JTextField qtyField = new JTextField("1", 4);
        JButton addCartBtn = new JButton("Add to Cart");
        JButton checkoutBtn = new JButton("Checkout");
        JButton changePassBtn = new JButton("Change Password");
        JButton logoutBtn = new JButton("Logout");

        bottom.add(new JLabel("Qty:"));
        bottom.add(qtyField);
        bottom.add(addCartBtn);
        bottom.add(checkoutBtn);
        bottom.add(changePassBtn);
        bottom.add(logoutBtn);

        // Các tab sản phẩm, giỏ hàng và lịch sử đơn.
        JTabbedPane tabs = new JTabbedPane();
        cartArea.setEditable(false);
        orderHistoryArea.setEditable(false);
        tabs.addTab("1. Products", new JScrollPane(table));
        tabs.addTab("2. Cart", new JScrollPane(cartArea));
        tabs.addTab("3. Order History", new JScrollPane(orderHistoryArea));
        panel.add(tabs, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        // Chọn sản phẩm và thêm số lượng vào giỏ hàng.
        addCartBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a product in table!");
                return;
            }
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                Product prod = products.get(row);
                ((Customer) currentUser).getCart().addProduct(prod, qty);
                saveData();
                refreshData();
                JOptionPane.showMessageDialog(this, "Added to cart!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // Nối nút Checkout với luồng xử lý thanh toán và tạo đơn hàng.
        checkoutBtn.addActionListener(e -> {
            if (currentUser instanceof Customer) {
                checkout((Customer) currentUser, tabs);
            }
        });

        // Đổi mật khẩu và đăng xuất.
        changePassBtn.addActionListener(e -> changePasswordDialog());
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        return panel;
    }

    // THANH TOÁN, TẠO ĐƠN HÀNG VÀ LỊCH SỬ ĐƠN
    // Lớp nghiệp vụ liên quan: CheckoutService.java, Order.java, Payment.java.
    private void checkout(Customer customer, JTabbedPane tabs) {
        Cart cart = customer.getCart();
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }

        String[] methods = {"Cash on delivery", "Bank transfer (demo)"};
        int choice = JOptionPane.showOptionDialog(this, "Choose payment method:", "Checkout",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, methods, methods[0]);
        if (choice < 0) return;

        try {
            Order order = checkoutService.checkout(customer, choice);
            saveData();
            refreshData();
            tabs.setSelectedIndex(2);
            JOptionPane.showMessageDialog(this, "Order placed! ID: " + order.getId());
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, "Checkout failed: " + ex.getMessage());
        }
    }

    // ==================== DỮ LIỆU HIỂN THỊ DÙNG CHUNG ====================
    // Cập nhật bảng sản phẩm; phần giỏ hàng và lịch sử dùng dữ liệu của Customer.
    private void refreshData() {
        productModel.setRowCount(0);
        adminProductModel.setRowCount(0);
        for (Product p : products) {
            Object[] row = {p.getId(), p.getName(), p.getPrice(), p.getStock()};
            productModel.addRow(row);
            adminProductModel.addRow(row);
        }

        if (currentUser instanceof Customer) {
            Customer c = (Customer) currentUser;

            // Cập nhật nội dung giỏ hàng.
            StringBuilder cartText = new StringBuilder("--- YOUR CART ---\n");
            for (CartItem item : c.getCart().getItems()) {
                cartText.append(item.getProduct().getName())
                        .append(" x ").append(item.getQuantity())
                        .append(" = ").append(item.getSubtotal()).append(" VND\n");
            }
            cartText.append("Total: ").append(c.getCart().getTotal()).append(" VND");
            cartArea.setText(cartText.toString());

            // Cập nhật lịch sử đơn hàng và trạng thái thanh toán.
            StringBuilder orderText = new StringBuilder("--- ORDER HISTORY ---\n");
            for (Order o : c.getOrderHistory()) {
                orderText.append("Order ID: ").append(o.getId())
                        .append(" | Total: ").append(o.getPayment().getAmount()).append(" VND")
                        .append(" | Method: ").append(o.getPayment().getMethod())
                        .append(" | Status: ").append(o.getPayment().isPaid() ? "PAID" : "UNPAID").append("\n");
                for (OrderItem item : o.getItems()) {
                    orderText.append("   + ").append(item).append("\n");
                }
                orderText.append("------------------------------\n");
            }
            orderHistoryArea.setText(orderText.toString());
        }
    }

    // ==================== ĐIỂM KHỞI ĐỘNG CHƯƠNG TRÌNH ====================
    public static void main(String[] args) {
        new ShoppingSwingApp().setVisible(true);
    }
}
