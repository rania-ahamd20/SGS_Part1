import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/data_db", "user", "pass")) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your username: ");
            String username = scanner.nextLine();
            System.out.println("Enter your password: ");
            String password = scanner.nextLine();

            if (authenticateUser(connection, username, password)) {
                retrieveMarks(connection, username);
            } else {
                System.out.println("Authentication failed. Exiting...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean authenticateUser(Connection connection, String username, String password) throws SQLException {
        String query = "SELECT * FROM students WHERE username = ? AND password = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();  // If a row is returned, the user is authenticated
        }
    }

    private static void retrieveMarks(Connection connection, String username) throws SQLException {
        String query = "SELECT courses.course_name, grades_2.mark " +
                "FROM students " +
                "JOIN grades_2 ON students.student_id = grades_2.student_id " +
                "JOIN courses ON grades_2.course_id = courses.course_id " +
                "WHERE students.username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display marks
            System.out.println("Marks for " + username + ":");
            while (resultSet.next()) {
                String courseName = resultSet.getString("course_name");
                int mark = resultSet.getInt("mark");

                System.out.println(courseName + ": " + mark);
            }
        }
    }
}
