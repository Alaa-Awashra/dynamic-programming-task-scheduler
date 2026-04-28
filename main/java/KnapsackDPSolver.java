public class KnapsackDPSolver {

    public DPResult solve(TaskDataset dataset) {
        MyArrayList<Task> tasks = dataset.getTasks();
        int n = tasks.size();
        int W = dataset.getTotalHoursUnits(); // capacity in half-hour units

        // 1D DP: dp[w] = max value achievable within capacity w
        int[] dp = new int[W + 1];

        // Snapshots (still 1D) for optional display and for reconstruction by before/after comparison
        MyArrayList<int[]> rows = new MyArrayList<>();
        rows.add(cloneArray(dp)); // row 0: before any task

        // Build DP
        for (int i = 0; i < n; i++) {
            Task t = tasks.get(i);
            int wi = FileHandler.toUnits(t.getHours());
            int vi = t.getValue();

            for (int w = W; w >= wi; w--) {
                int take = dp[w - wi] + vi;
                if (take > dp[w]) {
                    dp[w] = take;
                }
            }

            rows.add(cloneArray(dp)); // ؤ after task i ,row i+1
        }

        // Reconstruct chosen tasks using snapshot comparison (before vs after each task)
        MyArrayList<Task> chosen = new MyArrayList<>();
        int w = W;
        int used = 0;

        for (int i = n - 1; i >= 0; i--) {
            int[] after = rows.get(i + 1); // dp after processing task i
            int[] before = rows.get(i);    // dp before processing task i

            if (after[w] != before[w]) {
                Task t = tasks.get(i);
                int wi = FileHandler.toUnits(t.getHours());
                chosen.add(t);
                used += wi;
                w -= wi;
                if (w <= 0)
                    break;
            }
        }

        reverseInPlace(chosen);

        String recurrence =
                "Let f[i][w] be the maximum productivity using the first i tasks within w half-hour units.\n" +
                        "If weight_i <= w:\n" +
                        "  f[i][w] = max( f[i-1][w], f[i-1][w-weight_i] + value_i )\n" +
                        "Else:\n" +
                        "  f[i][w] = f[i-1][w]\n";

        return new DPResult(rows, chosen, used, dp[W], recurrence);
    }

    // Make a copy of an int array
    private int[] cloneArray(int[] a) {
        int[] b = new int[a.length];
        for (int i = 0; i < a.length; i++) b[i] = a[i];
        return b;
    }

    // Reverse a MyArrayList<Task> in place
    private void reverseInPlace(MyArrayList<Task> list) {
        int i = 0, j = list.size() - 1;
        while (i < j) {
            Task tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
            i++;
            j--;
        }
    }
}
