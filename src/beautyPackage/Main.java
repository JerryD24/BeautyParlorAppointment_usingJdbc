package beautyPackage;

import java.sql.*;
import java.util.Scanner;

import static beautyPackage.DatabaseConnector.*;


public class Main {


	public static void main(String[] args) throws SQLException {
        System.out.println("Started");

        DatabaseConnector connector = new DatabaseConnector();
        Connection connection = connector.getConnection();

        Main app = new Main();
        app.run(connection);

        connector.close();

        System.out.println("Finish");
    }

    private void run(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Welcome to the Beauty Parlor Appointment System! What would you like to do?");
            System.out.println("1. Register a new customer");
            System.out.println("2. Book an appointment");
            System.out.println("3. View my appointment history");
            System.out.println("4. Modify my appointment");
            System.out.println("5. Cancel my appointment");
            System.out.println("6. View the list of beauticians");
            System.out.println("7. View the list of services");
            System.out.println("0. Exit");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    Main.registerCustomer(connection, scanner);
                    break;
                case 2:
                    Main.bookAppointment(connection, scanner);
                    break;
                case 3:
                    Main.viewAppointmentHistory(connection, scanner);
                    break;
                case 4:
                    Main.modifyAppointment(connection, scanner);
                    break;
                case 5:
                    Main.cancelAppointment(connection, scanner);
                    break;
                case 6:
                    Main.viewBeauticians(connection, scanner);
                    break;
                case 7:
                    Main.viewServices(connection, scanner);
                    break;
                case 0:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice");
            }
        }
    }

    private static void registerCustomer(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter your name:");
        String name = scanner.nextLine();

        System.out.println("Please enter your email address:");
        String email = scanner.nextLine();

        System.out.println("Please enter your phone number:");
        String phone = scanner.nextLine();

        System.out.println("Please enter your address:");
        String address = scanner.nextLine();

        try (Connection customerConnection = DriverManager.getConnection(CONNECTION_STRING, USERNAME, PASSWORD);
             PreparedStatement statement = customerConnection.prepareStatement("INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)")
        ) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, address);
            statement.executeUpdate();
            System.out.println("Customer registered successfully!");
        }
        System.out.println();
    }
    private static void viewAppointmentHistory(Connection connection, Scanner scanner) throws SQLException {

        String sql = "SELECT * FROM appointments WHERE id = ?";
        int tak = Integer.parseInt(scanner.nextLine());	//String to integer
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int id = tak;
            statement.setInt(1, id);
            ResultSet results = statement.executeQuery();

            if (results != null) {
                while (results.next()) {
                    id = results.getInt("id");
                    int beauticianId = results.getInt("beautician_id");
                    int serviceId = results.getInt("service_id");
                    String date = results.getString("date");
                    String time = results.getString("time");

                    System.out.println("Id: " + id + ", Beautician Id: " + beauticianId + ", Service Id: " + serviceId + ", Date: " + date + ", Time: " + time);
                }
            } else {
                System.out.println("No appointments found");
            }
        }
        System.out.println();
    }

    private static void bookAppointment(Connection connection, Scanner scanner) {
        System.out.println("Please select a beautician:");
        viewBeauticians(connection, scanner);

        int beauticianId = Integer.parseInt(scanner.nextLine());

        System.out.println("Please select a service:");
        viewServices(connection, scanner);

        int serviceId = Integer.parseInt(scanner.nextLine());

        System.out.println("Please enter the date of your appointment (YYYY-MM-DD):");
        String date = scanner.nextLine();

        System.out.println("Please enter the time of your appointment (HH:mm):");
        String time = scanner.nextLine();

        try {
            String sql = "INSERT INTO appointments (beautician_id, service_id, date, time) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, beauticianId);
            statement.setInt(2, serviceId);
            statement.setString(3, date);
            statement.setString(4, time);
            statement.executeUpdate();
            System.out.println("Appointment booked successfully!");
        } catch (SQLException e) {
            System.out.println("Error booking appointment: " + e.getMessage());
        }
        System.out.println();
    }


    private static void viewBeauticians(Connection connection, Scanner scanner) {
        try {
            String sql = "SELECT * FROM beauticians";
            ResultSet results = connection.createStatement().executeQuery(sql);

            while (results.next()) {
                int id = results.getInt("id");
                String name = results.getString("name");
                String specialization = results.getString("specialization");

                System.out.println("Id: " + id + ", Name: " + name + ", Specialization: " + specialization);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing beauticians: " + e.getMessage());
        }
        System.out.println();
    }

    private static void viewServices(Connection connection, Scanner scanner) {
        try {
            String sql = "SELECT * FROM services";
            ResultSet results = connection.createStatement().executeQuery(sql);

            while (results.next()) {
                int id = results.getInt("id");
                String name = results.getString("name");
                double price = results.getDouble("price");

                System.out.println("Id: " + id + ", Name: " + name + ", Price: " + price);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing services: " + e.getMessage());
        }
        System.out.println();
    }
    private static void modifyAppointment(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter the id of the appointment you want to modify:");
        int id = Integer.parseInt(scanner.nextLine());

        String selectSql = "SELECT * FROM appointments WHERE id = ?";
        String updateSql = "UPDATE appointments SET date = ?, time = ? WHERE id = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            selectStatement.setInt(1, id);
            ResultSet results = selectStatement.executeQuery();

            if (results.next()) {
                System.out.println("Please enter the new date of your appointment (YYYY-MM-DD):");
                String date = scanner.nextLine();

                System.out.println("Please enter the new time of your appointment (HH:mm):");
                String time = scanner.nextLine();

                updateStatement.setString(1, date);
                updateStatement.setString(2, time);
                updateStatement.setInt(3, id);
                updateStatement.executeUpdate();

                System.out.println("Appointment modified successfully!");
            } else {
                System.out.println("Appointment not found");
            }
        }
        System.out.println();
    }





    private static void cancelAppointment(Connection connection, Scanner scanner) throws SQLException {
        int id = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM appointments WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Appointment canceled successfully!");
        }
        System.out.println();
    }
}