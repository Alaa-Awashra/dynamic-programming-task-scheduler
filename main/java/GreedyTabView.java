import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class GreedyTabView extends Tab {

    private final Controllers controller;

    private final TableView<Task> tasksTable = new TableView<>();
    private final TableView<GreedyDecisionRow> greedyTable = new TableView<>();
    private final Text resultsText = new Text("");

    public GreedyTabView(Controllers controller) {

        // Tab title
        super("Greedy");
        this.controller = controller;

        // Layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox controls = buildControlsBox();
        Parent main = buildMainArea();

        root.setCenter(main);
        root.setRight(controls);

        setContent(root);
    }

    // Method to build the controls box on the right
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

            double max = controller.getDataset().getTotalHoursAsDouble();
            Double newHours = UpdateHours.showUpdateHours(controller.getOwnerStage(), selected);
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
        box.setPadding(new Insets(10));
        box.setPrefWidth(200);
        return box;
    }

    // Method to build the main area in the center
    private Parent buildMainArea() {
        setupTasksTable();

        // Bind once to the controller observable list
        tasksTable.setItems(controller.getTasksView());

        tasksTable.setFixedCellSize(28);
        tasksTable.setPrefHeight(28 * 5 + 30);
        tasksTable.setMinHeight(tasksTable.getPrefHeight());
        tasksTable.setMaxHeight(tasksTable.getPrefHeight());
        VBox.setVgrow(tasksTable, Priority.NEVER);

        greedyTable.setPlaceholder(new Label("Greedy decision table will appear here."));
        greedyTable.getStyleClass().add("matrix-table");
        greedyTable.setFixedCellSize(28);
        VBox.setVgrow(greedyTable, Priority.ALWAYS);

        Button runGreedyBtn = new Button("Run Greedy");
        runGreedyBtn.disableProperty().bind(controller.fileLoadedProperty().not());
        runGreedyBtn.setOnAction(e -> {
            try {
                GreedySolver solver = new GreedySolver();

                long start = System.nanoTime();
                GreedyResult res = solver.solve(controller.getDataset());
                long end = System.nanoTime();
                double greedyMs = (end - start) / 1_000_000.0;

                setupGreedyTable();
                populateGreedyDecisionTable(res);
                resultsText.setText(buildGreedyText(res, greedyMs));
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        Label tasksTitle = new Label("Tasks (from file)");
        tasksTitle.getStyleClass().add("section-title");

        Label gTitle = new Label("Greedy Table");
        gTitle.getStyleClass().add("section-title");

        Label resTitle = new Label("Results");
        resTitle.getStyleClass().add("section-title");

        VBox box = new VBox(10, tasksTitle, tasksTable, gTitle, greedyTable, runGreedyBtn, resTitle, resultsText);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("card");

        return box;
    }

    // Method to setup the tasks table columns
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

    // Method to setup the greedy decision table columns
    private void setupGreedyTable() {
        greedyTable.getColumns().clear();

        TableColumn<GreedyDecisionRow, Integer> stepCol = new TableColumn<>("Step");
        stepCol.setCellValueFactory(new PropertyValueFactory<>("step"));

        TableColumn<GreedyDecisionRow, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<GreedyDecisionRow, String> nameCol = new TableColumn<>("Task");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<GreedyDecisionRow, Double> hCol = new TableColumn<>("Hours");
        hCol.setCellValueFactory(new PropertyValueFactory<>("hours"));

        TableColumn<GreedyDecisionRow, Integer> vCol = new TableColumn<>("Value");
        vCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        TableColumn<GreedyDecisionRow, Double> remCol = new TableColumn<>("Remaining");
        remCol.setCellValueFactory(new PropertyValueFactory<>("remainingHours"));

        TableColumn<GreedyDecisionRow, String> takenCol = new TableColumn<>("Taken");
        takenCol.setCellValueFactory(new PropertyValueFactory<>("taken"));

        greedyTable.getColumns().addAll(stepCol, idCol, nameCol, hCol, vCol, remCol, takenCol);
        greedyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        greedyTable.setFixedCellSize(28);
        greedyTable.refresh();
    }

    // Method to populate the greedy decision table
    private void populateGreedyDecisionTable(GreedyResult res) {
        greedyTable.getItems().clear();
        MyArrayList<GreedyDecisionRow> rows = res.getDecisions();
        for (int i = 0; i < rows.size(); i++) {
            greedyTable.getItems().add(rows.get(i));
        }
    }

    // Method to build the results text for greedy
    private String buildGreedyText(GreedyResult res, double greedyMs) {
        StringBuilder sb = new StringBuilder();

        sb.append("Greedy Selected Tasks:\n");
        if (res.getSelected().size() == 0) sb.append("  (none)\n");
        for (int i = 0; i < res.getSelected().size(); i++) {
            Task t = res.getSelected().get(i);
            sb.append("  - ").append(t.getId()).append(": ").append(t.getName())
                    .append(" (").append(t.getHours()).append("h, value=").append(t.getValue()).append(")\n");
        }
        sb.append("Total Hours Used: ").append(FileHandler.fromUnits(res.getTotalUnitsUsed())).append("\n");
        sb.append("Total Productivity: ").append(res.getTotalValue()).append("\n");
        sb.append("Greedy Runtime: ").append(String.format("%.3f", greedyMs)).append(" ms\n");

        return sb.toString();
    }

    // Method to show information alerts
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.initOwner(controller.getOwnerStage());
        a.showAndWait();
    }

    // Method to show error alerts
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.initOwner(controller.getOwnerStage());
        a.showAndWait();
    }
}
