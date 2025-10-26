import java.sql.*;
import java.util.Scanner;

public class LibraryManagement {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root";
    private static final String PASS = ""; // Update with your MySQL password if needed

    private Connection conn;

    public LibraryManagement() {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            createTableIfNotExists();
            System.out.println("Connected to database successfully.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS books (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "title VARCHAR(255) NOT NULL, " +
                     "author VARCHAR(255) NOT NULL, " +
                     "available BOOLEAN DEFAULT TRUE)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    public void addBook(String title, String author) {
        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.executeUpdate();
            System.out.println("Book added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding book: " + e.getMessage());
        }
    }

    public void issueBook(int id) {
        String checkSql = "SELECT available FROM books WHERE id = ?";
        String updateSql = "UPDATE books SET available = FALSE WHERE id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getBoolean("available")) {
                updateStmt.setInt(1, id);
                updateStmt.executeUpdate();
                System.out.println("Book issued successfully.");
            } else {
                System.out.println("Book is not available or does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("Error issuing book: " + e.getMessage());
        }
    }

    public void returnBook(int id) {
        String sql = "UPDATE books SET available = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book returned successfully.");
            } else {
                System.out.println("Book does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("Error returning book: " + e.getMessage());
        }
    }

    public void listBooks() {
        String sql = "SELECT * FROM books";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("ID\tTitle\t\tAuthor\t\tAvailable");
            System.out.println("--------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%d\t%s\t\t%s\t\t%s%n",
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getBoolean("available") ? "Yes" : "No");
            }
        } catch (SQLException e) {
            System.out.println("Error listing books: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        LibraryManagement lm = new LibraryManagement();
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\nLibrary Management System");
            System.out.println("1. Add Book");
            System.out.println("2. Issue Book");
            System.out.println("3. Return Book");
            System.out.println("4. List Books");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter book title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter book author: ");
                    String author = scanner.nextLine();
                    lm.addBook(title, author);
                    break;
                case 2:
                    System.out.print("Enter book ID to issue: ");
                    int issueId = scanner.nextInt();
                    lm.issueBook(issueId);
                    break;
                case 3:
                    System.out.print("Enter book ID to return: ");
                    int returnId = scanner.nextInt();
                    lm.returnBook(returnId);
                    break;
                case 4:
                    lm.listBooks();
                    break;
                case 5:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 5);

        lm.closeConnection();
        scanner.close();
    }
}
