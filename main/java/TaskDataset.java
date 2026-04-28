public class TaskDataset { //groups the global constraint (total hours) with the tasks list into one object that can be passed around cleanly.

    private int totalHoursUnits; // half-hour units
    private MyArrayList<Task> tasks;

    public TaskDataset(int totalHoursUnits, MyArrayList<Task> tasks) {
        this.totalHoursUnits = totalHoursUnits;
        this.tasks = tasks;
    }

    public int getTotalHoursUnits() {
        return totalHoursUnits;
    }

    public void setTotalHoursUnits(int totalHoursUnits) {
        this.totalHoursUnits = totalHoursUnits;
    }

    public double getTotalHoursAsDouble() {
        return totalHoursUnits / 2.0;
    }

    public MyArrayList<Task> getTasks() {
        return tasks;
    }
}
