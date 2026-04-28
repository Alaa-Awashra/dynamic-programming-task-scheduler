
public class Task {
    // Task properties
    private int id;
    private String name;
    private double hours;
    private int value;

    // Constructor
    public Task(int id, String name, double hours, int value) {
        this.id = id;
        this.name = name;
        this.hours = hours;
        this.value = value;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getHours() {
        return hours;
    }
    public int getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setHours(double hours) {
        this.hours = hours;
    }
    public void setValue(int value) {
        this.value = value;
    }
}
