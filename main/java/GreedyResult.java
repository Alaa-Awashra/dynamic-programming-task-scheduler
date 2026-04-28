public class GreedyResult {
    private final MyArrayList<Task> selected;
    private final int totalUnitsUsed;
    private final int totalValue;
    private final MyArrayList<GreedyDecisionRow> decisions;

    public GreedyResult(MyArrayList<Task> selected, int totalUnitsUsed, int totalValue, MyArrayList<GreedyDecisionRow> decisions) {
        this.selected = selected;
        this.totalUnitsUsed = totalUnitsUsed;
        this.totalValue = totalValue;
        this.decisions = decisions;
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
    public MyArrayList<GreedyDecisionRow> getDecisions() {
        return decisions;
    }
}
