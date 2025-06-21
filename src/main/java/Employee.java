import java.util.Date;
import java.util.List;

public class Employee {
    private String id;
    private String name;
    private String email;
    private String department;
    private List<String> skills;
    private Date joiningDate;

    // Constructors, getters, and setters
    public Employee() {}

    public Employee(String name, String email, String department, List<String> skills, Date joiningDate) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.skills = skills;
        this.joiningDate = joiningDate;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public Date getJoiningDate() { return joiningDate; }
    public void setJoiningDate(Date joiningDate) { this.joiningDate = joiningDate; }
}