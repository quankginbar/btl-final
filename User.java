public abstract class User {
    private String id;
    private String username;
    private String password;

    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    // Dùng cho DataStorage khi lưu file (mật khẩu đang lưu dạng plain text).
    public String getPassword() {
        return password;
    }

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (password.equals(oldPassword) && newPassword != null && !newPassword.trim().isEmpty()) {
            password = newPassword;
            return true;
        }
        return false;
    }

    public abstract String getRole();
}
