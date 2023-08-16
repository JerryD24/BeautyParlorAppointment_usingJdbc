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
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.println("Welcome to the Beauty Parlor Appointment System! What would you like to do?");
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.println("1. Register a new customer");
            System.out.println("2. Modify Customer");
            System.out.println("3. Delete Customer");
            System.out.println("4. Register a beautician");
            System.out.println("5. modify a beautician");
            System.out.println("6. delete a beautician");
            System.out.println("7. View all the beauticians");
            System.out.println("8. Register a service");
            System.out.println("9. modify a service");
            System.out.println("10. Delete a service");
            System.out.println("11. View all the services");
            System.out.println("12. Book appointment");
            System.out.println("13. Modify Appointment");
            System.out.println("14. View Appointment");
            System.out.println("15. Cancel Appointment");
            System.out.println("0. Exit");
            System.out.println("---------------------------------------------------------------------------------------");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    Main.registerCustomer(connection, scanner);
                    break;
                case 2:
                    Main.modifyCustomer(connection, scanner);
                    break;
                case 3:
                    Main.deleteCustomer(connection, scanner);
                    break;
                case 4:
                    Main.addBeautician(connection, scanner);
                    break;
                case 5:
                    Main.modifyBeautician(connection, scanner);
                    break;
                case 6:
                    Main.deleteBeautician(connection, scanner);
                    break;
                case 7:
                    Main.viewBeauticians(connection, scanner);
                    break;
                case 8:
                    Main.addService(connection, scanner);
                    break;
                case 9:
                    Main.modifyService(connection, scanner);
                    break;
                case 10:
                    Main.deleteService(connection, scanner);
                    break;
                case 11:
                    Main.viewServices(connection, scanner);
                    break;
                case 12:
                    Main.bookAppointment(connection, scanner);
                    break;
                case 13:
                    Main.modifyAppointment(connection, scanner);
                    break;
                case 14:
                    Main.viewAppointmentHistory(connection, scanner);
                    break;
                case 15:
                    Main.cancelAppointment(connection, scanner);
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
             PreparedStatement statement = customerConnection.prepareStatement("INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, address);
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            int appointmentId = -1;
            if (generatedKeys.next()) {
                appointmentId = generatedKeys.getInt(1);
            }

            if (appointmentId != -1) {
                System.out.println("Customer registered successfully! Your Customer ID: " + appointmentId);
            } else {
                System.out.println("Failed to retrieve appointment ID.");
            }
        } catch (SQLException e) {
            System.out.println("Error in register customer: " + e.getMessage());
        }
        System.out.println();
    }

    private static void viewAppointmentHistory(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter Booking Appointment");
        String sql = "SELECT * FROM appointments WHERE id = ?";
        int tak = scanner.nextInt();
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
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, beauticianId);
            statement.setInt(2, serviceId);
            statement.setString(3, date);
            statement.setString(4, time);
            statement.executeUpdate();

            // Retrieve the generated appointment ID
            ResultSet generatedKeys = statement.getGeneratedKeys();
            int appointmentId = -1;
            if (generatedKeys.next()) {
                appointmentId = generatedKeys.getInt(1);
            }

            if (appointmentId != -1) {
                System.out.println("Appointment booked successfully! Appointment ID: " + appointmentId);
            } else {
                System.out.println("Failed to retrieve appointment ID.");
            }
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
    }

    private static void modifyAppointment(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter the id of the appointment you want to modify:");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

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
    }


    private static void cancelAppointment(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter Appointment ID:");

        int id = scanner.nextInt();

        String sql = "DELETE FROM appointments WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Appointment canceled successfully!");
        }
    }

    private static void modifyCustomer(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter the ID of the customer you want to modify:");
        int customerId = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String selectSql = "SELECT * FROM customers WHERE id = ?";
        String updateSql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE id = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            selectStatement.setInt(1, customerId);
            ResultSet results = selectStatement.executeQuery();

            if (results.next()) {
                System.out.println("Please enter the new name:");
                String name = scanner.nextLine();

                System.out.println("Please enter the new email address:");
                String email = scanner.nextLine();

                System.out.println("Please enter the new phone number:");
                String phone = scanner.nextLine();

                System.out.println("Please enter the new address:");
                String address = scanner.nextLine();

                updateStatement.setString(1, name);
                updateStatement.setString(2, email);
                updateStatement.setString(3, phone);
                updateStatement.setString(4, address);
                updateStatement.setInt(5, customerId);
                updateStatement.executeUpdate();

                System.out.println("Customer information modified successfully!");
            } else {
                System.out.println("Customer not found");
            }
        }
    }

    private static void deleteCustomer(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter Customer ID to delete:");
        int customerId = scanner.nextInt();

        String deleteCustomerSql = "DELETE FROM customers WHERE id = ?";
        String deleteAppointmentsSql = "DELETE FROM appointments WHERE id = ?";

        try (PreparedStatement deleteAppointmentsStatement = connection.prepareStatement(deleteAppointmentsSql);
             PreparedStatement deleteCustomerStatement = connection.prepareStatement(deleteCustomerSql)) {

            // Delete customer's appointments first
            deleteAppointmentsStatement.setInt(1, customerId);
            deleteAppointmentsStatement.executeUpdate();

            // Then delete the customer
            deleteCustomerStatement.setInt(1, customerId);
            int rowsAffected = deleteCustomerStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Customer and their appointments deleted successfully!");
            } else {
                System.out.println("Customer not found or an error occurred.");
            }
        }
    }

    private static void addBeautician(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter the ID of the beautician:");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        System.out.println("Please enter the name of the beautician:");
        String name = scanner.nextLine();

        System.out.println("Please enter the specialization of the beautician:");
        String specialization = scanner.nextLine();

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO beauticians (id, name, specialization) VALUES (?, ?, ?)")) {

            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, specialization);
            statement.executeUpdate();

            System.out.println("Beautician added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding beautician: " + e.getMessage());
        }
        System.out.println();
    }

    private static void deleteBeautician(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter the ID of the beautician to delete:");
        int id = scanner.nextInt();

        String deleteSql = "DELETE FROM beauticians WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Beautician deleted successfully!");
            } else {
                System.out.println("Beautician not found or an error occurred.");
            }
        }
    }

    private static void modifyBeautician(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter the ID of the beautician you want to modify:");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String selectSql = "SELECT * FROM beauticians WHERE id = ?";
        String updateSql = "UPDATE beauticians SET name = ?, specialization = ? WHERE id = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            selectStatement.setInt(1, id);
            ResultSet results = selectStatement.executeQuery();

            if (results.next()) {
                System.out.println("Please enter the new name:");
                String name = scanner.nextLine();

                System.out.println("Please enter the new specialization:");
                String specialization = scanner.nextLine();

                updateStatement.setString(1, name);
                updateStatement.setString(2, specialization);
                updateStatement.setInt(3, id);
                updateStatement.executeUpdate();

                System.out.println("Beautician information modified successfully!");
            } else {
                System.out.println("Beautician not found");
            }
        }
    }

    private static void addService(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter the service details:");

        System.out.println("Enter Service ID (manually assigned):");
        int serviceId = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        System.out.println("Enter Service Name:");
        String serviceName = scanner.nextLine();

        System.out.println("Enter Service Price:");
        double servicePrice = scanner.nextDouble();
        scanner.nextLine(); // Consume newline character

        try {
            String sql = "INSERT INTO services (id, name, price) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, serviceId);
            statement.setString(2, serviceName);
            statement.setDouble(3, servicePrice);
            statement.executeUpdate();

            System.out.println("Service added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding service: " + e.getMessage());
        }
        System.out.println();
    }

    private static void modifyService(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Please enter the ID of the service you want to modify:");
        int serviceId = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String selectSql = "SELECT * FROM services WHERE id = ?";
        String updateSql = "UPDATE services SET name = ?, price = ? WHERE id = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            selectStatement.setInt(1, serviceId);
            ResultSet results = selectStatement.executeQuery();

            if (results.next()) {
                System.out.println("Please enter the new name:");
                String name = scanner.nextLine();

                System.out.println("Please enter the new price:");
                double price = scanner.nextDouble();
                scanner.nextLine(); // Consume newline character

                updateStatement.setString(1, name);
                updateStatement.setDouble(2, price);
                updateStatement.setInt(3, serviceId);
                updateStatement.executeUpdate();

                System.out.println("Service information modified successfully!");
            } else {
                System.out.println("Service not found");
            }
        }
    }

    private static void deleteService(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter Service ID to delete:");
        int serviceId = scanner.nextInt();

        String deleteSql = "DELETE FROM services WHERE id = ?";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setInt(1, serviceId);
            int rowsAffected = deleteStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Service deleted successfully!");
            } else {
                System.out.println("Service not found or an error occurred.");
            }
        }
    }
}