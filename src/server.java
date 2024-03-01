import java.io.*;
import java.net.*;
import java.sql.*;

public class server {

    private static Connection connection;

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/data_db", "user", "pass");
            startServer();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is running. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                String[] commandParts = inputLine.split("\\s+");
                switch (commandParts[0]) {
                    case "GET_MARKS":
                        String usernameToRetrieve = commandParts[1];
                        sendMarksToClient(writer, usernameToRetrieve);
                        break;
                    default:
                        writer.println("Invalid command");
                        break;
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void sendMarksToClient(PrintWriter writer, String username) throws SQLException {
        String query = "SELECT courses.course_name, grades_2.mark " +
                "FROM students " +
                "JOIN grades_2 ON students.student_id = grades_2.student_id " +
                "JOIN courses ON grades_2.course_id = courses.course_id " +
                "WHERE students.username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Send marks to the client
            writer.println("Marks for " + username + ":");
            while (resultSet.next()) {
                String courseName = resultSet.getString("course_name");
                int mark = resultSet.getInt("mark");

                writer.println(courseName + ": " + mark);
            }
        } catch (SQLException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            writer.println("Error fetching marks");
        }
    }
}
