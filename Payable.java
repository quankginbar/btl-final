// Nguyên lý I (Interface Segregation) & D (Dependency Inversion) trong SOLID:
// Interface nhỏ gọn chỉ định nghĩa đúng hành vi thanh toán.
public interface Payable {
    void processPayment();
    boolean isPaid();
}

