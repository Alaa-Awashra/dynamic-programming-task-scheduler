public class DPResult {
    private final MyArrayList<int[]> dpRows;   // snapshots (row 0..n)
    private final MyArrayList<Task> selected;
    private final int totalUnitsUsed;
    private final int totalValue;
    private final String recurrence;

    public DPResult(MyArrayList<int[]> dpRows, MyArrayList<Task> selected, int totalUnitsUsed, int totalValue, String recurrence) {
        this.dpRows = dpRows;
        this.selected = selected;
        this.totalUnitsUsed = totalUnitsUsed;
        this.totalValue = totalValue;
        this.recurrence = recurrence;
    }

    // Getters
    public MyArrayList<int[]> getDpRows() {
        return dpRows;
    }

    public MyArrayList<Task> getSelected() {
        return selected;
    }
    public int getTotalUnitsUsed() {
        return totalUnitsUsed;
    }
    public int getTotalValue() {
        return totalValue;
    }
    public String getRecurrence() {
        return recurrence;
    }
}
