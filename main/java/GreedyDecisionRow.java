public class GreedyDecisionRow {
    private final int step;
    private final int id;
    private final String name;
    private final double hours;
    private final int value;
    private final double remainingHours;
    private final String taken;

    public GreedyDecisionRow(int step, Task t, double remainingHours, boolean taken) {
        this.step = step;
        this.id = t.getId();
        this.name = t.getName();
        this.hours = t.getHours();
        this.value = t.getValue();
        this.remainingHours = remainingHours;
        this.taken = taken ? "Yes" : "No";
    }

    // Getters
    public int getStep() {
        return step;
    }
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
    public double getRemainingHours() {
        return remainingHours;
    }
    public String getTaken() {
        return taken;
    }
}
