import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class EmployeeService {
    private final MongoCollection<Document> collection;

    public EmployeeService() {
        this.collection = MongoDBConnection.getDatabase().getCollection("employees");
    }

    public String addEmployee(Employee emp) {
        Document doc = new Document()
                .append("name", emp.getName())
                .append("email", emp.getEmail())
                .append("department", emp.getDepartment())
                .append("skills", emp.getSkills())
                .append("joiningDate", emp.getJoiningDate());

        collection.insertOne(doc);
        return doc.getObjectId("_id").toString();
    }

    public boolean updateEmployee(String id, String field, Object value) {
        UpdateResult result = collection.updateOne(
                eq("_id", new ObjectId(id)),
                new Document("$set", new Document(field, value))
        );
        return result.getModifiedCount() > 0;
    }

    public boolean deleteEmployeeById(String id) {
        DeleteResult result = collection.deleteOne(eq("_id", new ObjectId(id)));
        return result.getDeletedCount() > 0;
    }

    public boolean deleteEmployeeByEmail(String email) {
        DeleteResult result = collection.deleteOne(eq("email", email));
        return result.getDeletedCount() > 0;
    }

    public List<Employee> searchByName(String name) {
        return search(regex("name", name, "i"));
    }

    public List<Employee> searchByDepartment(String department) {
        return search(eq("department", department));
    }

    public List<Employee> searchBySkill(String skill) {
        return search(in("skills", skill));
    }

    public List<Employee> searchByJoiningDateRange(Date startDate, Date endDate) {
        return search(and(
                gte("joiningDate", startDate),
                lte("joiningDate", endDate)
        ));
    }

    public List<Document> getDepartmentStatistics() {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.group("$department", Accumulators.sum("count", 1))
        );
        return collection.aggregate(pipeline).into(new ArrayList<>());
    }

    public List<Employee> getAllEmployeesSorted(String field, boolean ascending) {
        Bson sort = ascending ? Sorts.ascending(field) : Sorts.descending(field);
        FindIterable<Document> sortedDocs = collection.find().sort(sort);
        List<Employee> employees = new ArrayList<>();
        for (Document doc : sortedDocs) {
            employees.add(toEmployee(doc));
        }
        return employees;
    }

    private List<Employee> search(Bson filter) {
        List<Employee> employees = new ArrayList<>();
        for (Document doc : collection.find(filter)) {
            employees.add(toEmployee(doc));
        }
        return employees;
    }

    private Employee toEmployee(Document doc) {
        Employee emp = new Employee();
        emp.setId(doc.getObjectId("_id").toString());
        emp.setName(doc.getString("name"));
        emp.setEmail(doc.getString("email"));
        emp.setDepartment(doc.getString("department"));
        emp.setSkills((List<String>) doc.get("skills")); // Optional: check type safety
        emp.setJoiningDate(doc.getDate("joiningDate"));
        return emp;
    }
}
