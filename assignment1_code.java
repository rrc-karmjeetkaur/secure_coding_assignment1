import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;

public class VulnerableApp {

    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://mydatabase.com/mydb");
private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = "secret123";

    public static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String input = scanner.nextLine();

    // Check for empty input
    if (input == null || input.isBlank()) {
        throw new IllegalArgumentException("Input cannot be empty");
    }

    // Allow only letters, spaces, hyphens, and apostrophes; limit to 100 characters
    if (!input.matches("^[A-Za-z\\-\\' ]{1,100}$")) {
        throw new IllegalArgumentException("Invalid characters in input. Please enter a valid name.");
    }

    return input.trim();
    }

    public static void sendEmail(String to, String subject, String body) {
        try {
            String command = String.format("echo %s | mail -s \"%s\" %s", body, subject, to);
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }

   public static String getData() {
    StringBuilder result = new StringBuilder();
    try {
        // Require HTTPS to avoid tampering
        URL url = new URL("https://insecure-api.com/get-data");

        // Validate protocol
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new IllegalStateException("Insecure protocol used; require HTTPS");
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000); // Timeout for connection
        conn.setReadTimeout(5000);    // Timeout for reading
        conn.setInstanceFollowRedirects(true);

        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            System.err.println("Warning: unexpected response code: " + status);
            return "";
        }

        try (InputStream inputStream = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        // Basic content validation
        String fetched = result.toString();
        if (fetched.length() > 10000) {
            System.err.println("Warning: fetched data too large, rejecting.");
            return "";
        }

        return fetched;

    } catch (Exception e) {
        // Secure logging — no sensitive info
        System.err.println("Error fetching data (internal): " + e.toString());
        return "";
    }
}


    public static void saveToDb(String data) {
        String query = "INSERT INTO mytable (column1, column2) VALUES ('" + data + "', 'Another Value')";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(query);
            System.out.println("Data saved to database.");

        } catch (SQLException e) {
            System.err.println("Database error occurred. Please contact the administrator.");
        }
    }

    public static void main(String[] args) {
        String userInput = getUserInput();
        String data = getData();
        saveToDb(data);
        sendEmail("admin@example.com", "User Input", userInput);
    }
}