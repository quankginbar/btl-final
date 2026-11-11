# ShoppingOnlineSystem - Java Swing

A small Java Swing online shopping application for practicing core Java implementation.

## Milestone 4 requirements covered

- **Java classes and OOP:** `User`, `Customer`, `Admin`, `Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `Payment`, and `CheckoutService`.
- **Encapsulation:** fields are private and accessed through methods.
- **Inheritance and polymorphism:** `Customer` and `Admin` extend the abstract `User` class and implement `getRole()`.
- **Collections:** `ArrayList` stores users, products, cart items, and order history.
- **Exception handling:** `NumberFormatException` is caught when the user enters invalid numeric input; `IllegalArgumentException` is caught when a cart operation has invalid quantity or insufficient stock.
- **Runnable Swing application:** login, customer registration, product listing, cart, checkout, order history, password change, and admin product creation.
- **Checkout service:** creates orders, processes demo payments, updates stock, and clears the cart independently of the Swing interface.

## Requirements

- JDK 8 or later
- Terminal / Command Prompt

## Compile and run

Open PowerShell in the folder containing the `.java` files and run:

```powershell
javac -encoding UTF-8 *.java
java ShoppingSwingApp
```

## Data persistence

The app saves its data to text files in a `data/` folder next to the compiled application classes (or next to the JAR when run from a JAR). This location does not depend on the terminal's current directory. The folder is created automatically on the first run:

| File | Content (one record per line, fields separated by `|`) |
|---|---|
| `users.txt` | `role|id|username|password` |
| `products.txt` | `id|name|price|stock` |
| `orders.txt` | `ORDER|orderId|customerId|method|amount|paid` followed by `ITEM|orderId|productName|price|quantity` lines |
| `carts.txt` | `customerId|productId|quantity` |

- Data is loaded when the program starts and saved after every change (register, change password, add product, add to cart, checkout) and when the window is closed.
- To reset the system to the demo data, close the app and delete the `data/` folder.
- The logic lives in `DataStorage.java`.

## Demo accounts

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Customer | `quan` | `123456` |

You can register another customer from the main menu.

## Suggested manual tests

1. Log in as admin and enter invalid price or stock values: the app should show a validation message.
2. Log in with an incorrect password: login should fail.
3. Add a quantity of `0` or a negative quantity to the cart: the program should reject it.
4. Try adding more items than the available stock: the program should reject it.
5. Log in as admin and add a product, then view the product list.
6. Log in as customer, place an order, and check the order history.

## Limitations

- Data is saved to plain `.txt` files (see "Data persistence" below); there is no database.
- Payment is simulated; no real money is transferred.
- Passwords are stored as plain text for this beginner educational example and are not suitable for production use.
