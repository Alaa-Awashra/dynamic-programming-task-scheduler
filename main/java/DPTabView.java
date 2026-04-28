import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class DPTabView extends Tab {

    private final Controllers controller;

    private final TableView<Task> tasksTable = new TableView<>();
    private final TableView<DPValueRow> dpTable = new TableView<>();
    private final Text resultsText = new Text("");

    // Simple row model to print the final 1D dp array
    public static class DPValueRow {
        private final double capacityHours;
        private final int bestValue;

        public DPValueRow(double capacityHours, int bestValue) {
            this.capacityHours = capacityHours;
            this.bestValue = bestValue;
        }

        public double getCapacityHours() {
            return capacityHours;
        }

        public int getBestValue() {
            return bestValue;
        }
    }

    public DPTabView(Controllers controller) {
        super("Knapsack");
        this.controller = controller;

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox controls = buildControlsBox();
        Parent main = buildMainArea();

        root.setCenter(main);
        root.setRight(controls);

        setContent(root);
    }

    private VBox buildControlsBox() {
        Button addBtn = new Button("Add Task");
        Button delBtn = new Button("Delete Task");
        Button updBtn = new Button("Update Hours");
        Button saveBtn = new Button("Save");

        addBtn.setMaxWidth(Double.MAX_VALUE);
        delBtn.setMaxWidth(Double.MAX_VALUE);
        updBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        addBtn.disableProperty().bind(controller.fileLoadedProperty().not());
        delBtn.disableProperty().bind(controller.fileLoadedProperty().not());
        updBtn.disableProperty().bind(controller.fileLoadedProperty().not());
        saveBtn.disableProperty().bind(
                controller.fileLoadedProperty().not().or(controller.dirtyProperty().not())
        );

        addBtn.setOnAction(e -> {
            Task t = AddTask.show(controller.getOwnerStage(), controller.nextId());
            if (t != null) controller.addTask(t);
        });

        delBtn.setOnAction(e -> {
            Task selected = tasksTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a task first.");
                return;
            }
            controller.deleteTaskById(selected.getId());
        });

        updBtn.setOnAction(e -> {
            Task selected = tasksTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Select a task first.");
                return;
            }

            Double newHours = UpdateHours.showUpdateHours(
                    controller.getOwnerStage(),
                    selected
            );

            if (newHours != null) controller.updateTaskHours(selected.getId(), newHours);
        });

        saveBtn.setOnAction(e -> {
            try {
                controller.saveToFile();
                showInfo("Saved.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        VBox box = new VBox(10, addBtn, delBtn, updBtn, saveBtn);
        box.getStyleClass().add("left-controls");
        box.setPrefWidth(220);
        box.setPadding(new Insets(10));
        return box;
    }

    private Parent buildMainArea() {
        setupTasksTable();
        setupDpTable();

        tasksTable.setItems(controller.getTasksView());

        tasksTable.setFixedCellSize(28);
        tasksTable.setPrefHeight(28 * 5 + 30);
        tasksTable.setMinHeight(tasksTable.getPrefHeight());
        tasksTable.setMaxHeight(tasksTable.getPrefHeight());
        VBox.setVgrow(tasksTable, Priority.NEVER);

        dpTable.setPlaceholder(new Label("Final DP array (1D) will appear here."));
        dpTable.getStyleClass().add("matrix-table");
        dpTable.setFixedCellSize(28);
        VBox.setVgrow(dpTable, Priority.ALWAYS);

        Button runDpBtn = new Button("Run DP");
        runDpBtn.disableProperty().bind(controller.fileLoadedProperty().not());
        runDpBtn.setOnAction(e -> {
            try {
                KnapsackDPSolver solver = new KnapsackDPSolver();

                long dpStart = System.nanoTime();
                DPResult dpRes = solver.solve(controller.getDataset());
                long dpEnd = System.nanoTime();

                double dpMs = (dpEnd - dpStart) / 1_000_000.0;

                // Show final dp[] only (1D)
                MyArrayList<int[]> rows = dpRes.getDpRows();
                int[] finalDp = rows.get(rows.size() - 1);
                populateFinalDpArray(finalDp, controller.getDataset().getTotalHoursUnits());

                StringBuilder sb = new StringBuilder();
                sb.append("DP Optimal Schedule:\n");
                appendTasks(sb, dpRes.getSelected());
                sb.append("Total Hours Used: ").append(FileHandler.fromUnits(dpRes.getTotalUnitsUsed())).append("\n");
                sb.append("Total Productivity: ").append(dpRes.getTotalValue()).append("\n");
                sb.append("DP Runtime: ").append(String.format("%.3f", dpMs)).append(" ms\n");

                resultsText.setText(String.valueOf(sb));
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        Label tasksTitle = new Label("Tasks (from file)");
        tasksTitle.getStyleClass().add("section-title");

        Label dpTitle = new Label("Final DP Array (1D)");
        dpTitle.getStyleClass().add("section-title");

        Label resTitle = new Label("Results");
        resTitle.getStyleClass().add("section-title");

        VBox box = new VBox(10, tasksTitle, tasksTable, dpTitle, dpTable, runDpBtn, resTitle, resultsText);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("card");

        return box;
    }

    private void setupTasksTable() {
        TableColumn<Task, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);

        TableColumn<Task, String> nameCol = new TableColumn<>("Task");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Task, Double> hoursCol = new TableColumn<>("Hours");
        hoursCol.setCellValueFactory(new PropertyValueFactory<>("hours"));
        hoursCol.setPrefWidth(90);

        TableColumn<Task, Integer> valCol = new TableColumn<>("Value");
        valCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valCol.setPrefWidth(90);

        tasksTable.getColumns().setAll(idCol, nameCol, hoursCol, valCol);
        tasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupDpTable() {
        dpTable.getColumns().clear();

        TableColumn<DPValueRow, Double> capCol = new TableColumn<>("Capacity (Hours)");
        capCol.setCellValueFactory(new PropertyValueFactory<>("capacityHours"));
        capCol.setPrefWidth(170);

        TableColumn<DPValueRow, Integer> bestCol = new TableColumn<>("Best Value");
        bestCol.setCellValueFactory(new PropertyValueFactory<>("bestValue"));
        bestCol.setPrefWidth(150);

        dpTable.getColumns().addAll(capCol, bestCol);
        dpTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void populateFinalDpArray(int[] dp, int W) {
        dpTable.getItems().clear();
        for (int w = 0; w <= W; w++) {
            dpTable.getItems().add(new DPValueRow(FileHandler.fromUnits(w), dp[w]));
        }
        dpTable.refresh();
    }

    private void appendTasks(StringBuilder sb, MyArrayList<Task> tasks) {
        if (tasks.size() == 0) {
            sb.append("  (none)\n");
            return;
        }
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("  - ").append(t.getId()).append(": ").append(t.getName())
                    .append(" (").append(t.getHours()).append("h, value=").append(t.getValue()).append(")\n");
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.initOwner(controller.getOwnerStage());
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.initOwner(controller.getOwnerStage());
        a.showAndWait();
    }
}
