import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.io.*;
import java.text.*;
import java.util.*;
import static com.mongodb.client.model.Filters.*;

public class EmployeeManagementPortal {
    private static MongoCollection<Document> employees;
    private static Scanner scanner = new Scanner(System.in);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Employee class as inner class
    static class Employee {
        private String id;
        private String name;
        private String email;
        private String department;
        private List<String> skills;
        private Date joiningDate;

        public Employee(String name, String email, String department,
                        List<String> skills, Date joiningDate) {
            this.name = name;
            this.email = email;
            this.department = department;
            this.skills = skills;
            this.joiningDate = joiningDate;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getDepartment() { return department; }
        public List<String> getSkills() { return skills; }
        public Date getJoiningDate() { return joiningDate; }

        // Setter for ID
        public void setId(String id) { this.id = id; }
    }

    public static void main(String[] args) {
        try {
            // Initialize MongoDB connection
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            employees = mongoClient.getDatabase("employeeDB").getCollection("employees");
            employees.createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));

            // Main menu
            while (true) {
                System.out.println("\nEMPLOYEE MANAGEMENT PORTAL");
                System.out.println("1. Add Employee");
                System.out.println("2. View All Employees");
                System.out.println("3. Update Employee");
                System.out.println("4. Delete Employee");
                System.out.println("5. Search Employees");
                System.out.println("6. Department Statistics");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> addEmployee();
                    case 2 -> viewAllEmployees();
                    case 3 -> updateEmployee();
                    case 4 -> deleteEmployee();
                    case 5 -> searchEmployees();
                    case 6 -> viewDepartmentStats();
                    case 0 -> {
                        mongoClient.close();
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void addEmployee() {
        try {
            System.out.println("\nADD EMPLOYEE");
            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Department: ");
            String dept = scanner.nextLine();

            System.out.print("Skills (comma separated): ");
            List<String> skills = Arrays.asList(scanner.nextLine().split("\\s*,\\s*"));

            System.out.print("Joining Date (yyyy-MM-dd): ");
            Date date = dateFormat.parse(scanner.nextLine());

            Document doc = new Document()
                    .append("name", name)
                    .append("email", email)
                    .append("department", dept)
                    .append("skills", skills)
                    .append("joiningDate", date);

            employees.insertOne(doc);
            System.out.println("Employee added successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAllEmployees() {
        System.out.println("\nALL EMPLOYEES");
        employees.find().forEach(doc -> printEmployee(doc));
    }

    private static void updateEmployee() {
        try {
            System.out.println("\nUPDATE EMPLOYEE");
            System.out.print("Enter Employee ID: ");
            String id = scanner.nextLine();

            System.out.println("Select field to update:");
            System.out.println("1. Name");
            System.out.println("2. Email");
            System.out.println("3. Department");
            System.out.println("4. Skills");
            System.out.println("5. Joining Date");
            System.out.print("Your choice: ");
            int field = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter new value: ");
            String value = scanner.nextLine();

            String fieldName = switch (field) {
                case 1 -> "name";
                case 2 -> "email";
                case 3 -> "department";
                case 4 -> "skills";
                case 5 -> "joiningDate";
                default -> throw new Exception("Invalid field");
            };

            Object updateValue = field == 4 ? Arrays.asList(value.split(",")) :
                    field == 5 ? dateFormat.parse(value) : value;

            employees.updateOne(
                    eq("_id", new ObjectId(id)),
                    new Document("$set", new Document(fieldName, updateValue))
            );
            System.out.println("Employee updated!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteEmployee() {
        System.out.println("\nDELETE EMPLOYEE");
        System.out.print("Enter Employee ID: ");
        String id = scanner.nextLine();

        employees.deleteOne(eq("_id", new ObjectId(id)));
        System.out.println("Employee deleted!");
    }

    private static void searchEmployees() {
        System.out.println("\nSEARCH EMPLOYEES");
        System.out.println("1. By Name");
        System.out.println("2. By Department");
        System.out.println("3. By Skill");
        System.out.println("4. By Joining Date");
        System.out.print("Your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            Bson filter;
            switch (choice) {
                case 1 -> {
                    System.out.print("Enter name: ");
                    filter = regex("name", scanner.nextLine(), "i");
                }
                case 2 -> {
                    System.out.print("Enter department: ");
                    filter = eq("department", scanner.nextLine());
                }
                case 3 -> {
                    System.out.print("Enter skill: ");
                    filter = in("skills", scanner.nextLine());
                }
                case 4 -> {
                    System.out.print("Enter start date (yyyy-MM-dd): ");
                    Date start = dateFormat.parse(scanner.nextLine());
                    System.out.print("Enter end date (yyyy-MM-dd): ");
                    Date end = dateFormat.parse(scanner.nextLine());
                    filter = and(gte("joiningDate", start), lte("joiningDate", end));
                }
                default -> throw new Exception("Invalid choice");
            }

            System.out.println("\nSEARCH RESULTS:");
            employees.find(filter).forEach(doc -> printEmployee(doc));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewDepartmentStats() {
        List<Document> pipeline = Arrays.asList(
                new Document("$group",
                        new Document("_id", "$department")
                                .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1))
        );

        System.out.println("\nDEPARTMENT STATISTICS:");
        employees.aggregate(pipeline).forEach(doc ->
                System.out.println(doc.getString("_id") + ": " + doc.getInteger("count"))
        );
    }

    private static void printEmployee(Document doc) {
        System.out.println("\nID: " + doc.getObjectId("_id"));
        System.out.println("Name: " + doc.getString("name"));
        System.out.println("Email: " + doc.getString("email"));
        System.out.println("Department: " + doc.getString("department"));
        System.out.println("Skills: " + doc.getList("skills", String.class));
        System.out.println("Joining Date: " + dateFormat.format(doc.getDate("joiningDate")));
        System.out.println("----------------------");
    }
}