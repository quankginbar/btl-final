import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lưu và đọc dữ liệu của hệ thống bằng các file .txt (UTF-8) trong một thư mục.
 *
 * Mỗi dòng là một bản ghi, các trường cách nhau bằng dấu '|':
 *   users.txt     : role|id|username|password
 *   products.txt  : id|name|price|stock
 *   orders.txt    : ORDER|orderId|customerId|method|amount|paid
 *                   ITEM|orderId|productName|price|quantity
 *   carts.txt     : customerId|productId|quantity
 *
 * Ký tự '|', '\' và xuống dòng trong dữ liệu được escape nên tên sản phẩm
 * hay mật khẩu chứa các ký tự này vẫn lưu/đọc đúng.
 */
public class DataStorage {
    private final Path usersFile;
    private final Path productsFile;
    private final Path ordersFile;
    private final Path cartsFile;

    public DataStorage(String folder) {
        Path dir = Paths.get(folder);
        if (!dir.isAbsolute()) {
            dir = applicationDirectory().resolve(dir);
        }
        usersFile = dir.resolve("users.txt");
        productsFile = dir.resolve("products.txt");
        ordersFile = dir.resolve("orders.txt");
        cartsFile = dir.resolve("carts.txt");
    }

    private static Path applicationDirectory() {
        if (DataStorage.class.getProtectionDomain() == null
                || DataStorage.class.getProtectionDomain().getCodeSource() == null) {
            throw new IllegalStateException("Could not determine the application location.");
        }
        try {
            Path location = Paths.get(
                    DataStorage.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isDirectory(location) ? location : location.getParent();
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Could not determine the application location.", ex);
        }
    }

    /** Đã có dữ liệu lưu từ lần chạy trước hay chưa. */
    public boolean exists() {
        return Files.exists(usersFile) && Files.exists(productsFile);
    }

    // ==================== GHI DỮ LIỆU ====================
    public void save(List<User> users, List<Product> products) throws IOException {
        List<String> userLines = new ArrayList<>();
        List<String> productLines = new ArrayList<>();
        List<String> orderLines = new ArrayList<>();
        List<String> cartLines = new ArrayList<>();

        for (User u : users) {
            userLines.add(join(u.getRole(), u.getId(), u.getUsername(), u.getPassword()));
            if (!(u instanceof Customer)) continue;

            Customer c = (Customer) u;
            for (Order o : c.getOrderHistory()) {
                Payment p = o.getPayment();
                orderLines.add(join("ORDER", o.getId(), o.getCustomerId(), p.getMethod(),
                        String.valueOf(p.getAmount()), String.valueOf(p.isPaid())));
                for (OrderItem item : o.getItems()) {
                    orderLines.add(join("ITEM", o.getId(), item.getProductName(),
                            String.valueOf(item.getPrice()), String.valueOf(item.getQuantity())));
                }
            }
            for (CartItem item : c.getCart().getItems()) {
                cartLines.add(join(c.getId(), item.getProduct().getId(),
                        String.valueOf(item.getQuantity())));
            }
        }

        for (Product p : products) {
            productLines.add(join(p.getId(), p.getName(),
                    String.valueOf(p.getPrice()), String.valueOf(p.getStock())));
        }

        Files.createDirectories(usersFile.getParent());
        write(usersFile, userLines);
        write(productsFile, productLines);
        write(ordersFile, orderLines);
        write(cartsFile, cartLines);
    }

    // ==================== ĐỌC DỮ LIỆU ====================
    /** Xóa nội dung hiện có của hai danh sách rồi nạp lại từ file. */
    public void load(List<User> users, List<Product> products) throws IOException {
        users.clear();
        products.clear();

        Map<String, Customer> customers = new HashMap<>();
        for (String[] f : read(usersFile)) {
            try {
                User user = f[0].equals("Admin")
                        ? new Admin(f[1], f[2], f[3])
                        : new Customer(f[1], f[2], f[3]);
                users.add(user);
                if (user instanceof Customer) customers.put(user.getId(), (Customer) user);
            } catch (RuntimeException ex) {
                skip(f);
            }
        }

        Map<String, Product> productsById = new HashMap<>();
        for (String[] f : read(productsFile)) {
            try {
                Product p = new Product(f[0], f[1], Double.parseDouble(f[2]), Integer.parseInt(f[3]));
                products.add(p);
                productsById.put(p.getId(), p);
            } catch (RuntimeException ex) {
                skip(f);
            }
        }

        // Đơn hàng: gom các dòng ITEM theo mã đơn, sau đó dựng Order từ dòng ORDER.
        List<String[]> orderHeaders = new ArrayList<>();
        Map<String, ArrayList<OrderItem>> itemsByOrder = new HashMap<>();
        for (String[] f : read(ordersFile)) {
            try {
                if (f[0].equals("ORDER")) {
                    orderHeaders.add(f);
                } else if (f[0].equals("ITEM")) {
                    itemsByOrder.computeIfAbsent(f[1], k -> new ArrayList<>())
                            .add(new OrderItem(f[2], Double.parseDouble(f[3]), Integer.parseInt(f[4])));
                }
            } catch (RuntimeException ex) {
                skip(f);
            }
        }
        for (String[] f : orderHeaders) {
            try {
                Customer customer = customers.get(f[2]);
                if (customer == null) continue;
                Payment payment = new Payment(f[3], Double.parseDouble(f[4]));
                if (Boolean.parseBoolean(f[5])) payment.processPayment();
                ArrayList<OrderItem> items = itemsByOrder.getOrDefault(f[1], new ArrayList<>());
                customer.addOrder(new Order(f[1], f[2], items, payment));
            } catch (RuntimeException ex) {
                skip(f);
            }
        }

        // Giỏ hàng: dùng đúng đối tượng Product đang có trong danh sách sản phẩm.
        for (String[] f : read(cartsFile)) {
            try {
                Customer customer = customers.get(f[0]);
                Product product = productsById.get(f[1]);
                if (customer != null && product != null) {
                    customer.getCart().addProduct(product, Integer.parseInt(f[2]));
                }
            } catch (RuntimeException ex) {
                skip(f);
            }
        }
    }

    // ==================== HÀM HỖ TRỢ ====================
    // Ghi ra file tạm rồi đổi tên để file cũ không bị hỏng nếu ghi dở chừng.
    private void write(Path file, List<String> lines) throws IOException {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(tmp, lines, StandardCharsets.UTF_8);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    private List<String[]> read(Path file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) return rows;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (!line.trim().isEmpty()) rows.add(split(line));
        }
        return rows;
    }

    private void skip(String[] fields) {
        System.err.println("Skipped invalid record: " + Arrays.toString(fields));
    }

    private static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append('|');
            sb.append(fields[i].replace("\\", "\\\\")
                    .replace("|", "\\|")
                    .replace("\r", "")
                    .replace("\n", "\\n"));
        }
        return sb.toString();
    }

    private static String[] split(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\\' && i + 1 < line.length()) {
                char next = line.charAt(++i);
                current.append(next == 'n' ? '\n' : next);
            } else if (ch == '|') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }
}
