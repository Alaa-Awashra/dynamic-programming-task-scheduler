public class GreedySolver {

    private final FileHandler fileHandler = new FileHandler();

    public GreedyResult solve(TaskDataset dataset) {
        MyArrayList<Task> tasks = dataset.getTasks();
        int W = dataset.getTotalHoursUnits();

        MyArrayList<Task> chosen = new MyArrayList<>();
        MyArrayList<GreedyDecisionRow> rows = new MyArrayList<>();

        int remainingUnits = W;
        int usedUnits = 0;
        int valueSum = 0;

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            int wi = FileHandler.toUnits(t.getHours());

            boolean take = (wi <= remainingUnits);

            if (take) {
                chosen.add(t);
                remainingUnits -= wi;
                usedUnits += wi;
                valueSum += t.getValue();
            }

            rows.add(new GreedyDecisionRow(
                    i + 1,                 // step number
                    t,                     // task
                    FileHandler.fromUnits(remainingUnits),
                    take
            ));
        }

        return new GreedyResult(chosen, usedUnits, valueSum, rows);
    }
}
