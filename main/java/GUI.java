import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class GUI {

    private final BorderPane root;
    private final Controllers controller;
    private final Label fileLabel;
    private final Label hoursLabel;

    // Creat the basic GUI structure
    public GUI(Stage stage) {

        controller = new Controllers(stage);
        this.root = new BorderPane();
        this.root.setPadding(new Insets(10));

        // Top bar layout
        HBox topBar = new HBox(10);
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(10));

        // Buttons and labels
        Button loadButton = new Button("Load File");
        Button editHoursButton = new Button("Edit Total Hours");

        this.fileLabel = new Label("No file loaded");
        this.hoursLabel = new Label("Total Hours: -");

        // Buttons actions
        loadButton.setOnAction(e -> controller.loadFile(fileLabel, hoursLabel));

        // only enabled after a file is loaded
        editHoursButton.disableProperty().bind(controller.fileLoadedProperty().not());

        editHoursButton.setOnAction(e -> {
            if (!controller.hasData())
                return;
            double current = controller.getDataset().getTotalHoursAsDouble();
            Double newTotal = editTotalHours(stage, current);

            if (newTotal != null) {
                try {
                    controller.updateTotalHours(newTotal, hoursLabel);
                } catch (Exception ex) {
                    Alert a = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                    a.initOwner(stage);
                    a.showAndWait();
                }
            }
        });

        // Arrange in top bar
        topBar.getChildren().addAll(loadButton, editHoursButton, fileLabel, hoursLabel);

        // Creating tab view
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new DPTabView(controller),
                new GreedyTabView(controller)
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.setTop(topBar);
        root.setCenter(tabPane);
    }


    private static Double editTotalHours(Window owner, double currentHours) {

        // Opens a window to edit total hours
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Edit Total Hours");

        // To use lamda later
        final Double[] result = new Double[1];

        // To control user input
        Spinner<Double> hoursSpinner = new Spinner<>();
        hoursSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 24.0, currentHours, 0.5)
        );
        hoursSpinner.setEditable(true);

        // normalize when leaving the field (forces 0.5 steps)
        hoursSpinner.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                Double v = hoursSpinner.getValue();
                double rounded = Math.round(v * 2.0) / 2.0;
                if (rounded < 0.5)
                    rounded = 0.5;
                if (rounded > 24)
                    rounded = 24;
                hoursSpinner.getValueFactory().setValue(rounded);
            }
        });

        // Buttons for OK and Cancel
        Button okBtn = new Button("OK");
        Button cancelBtn = new Button("Cancel");

        okBtn.setOnAction(e -> {
            result[0] = hoursSpinner.getValue();
            stage.close();
        });

        cancelBtn.setOnAction(e -> {
            result[0] = null;
            stage.close();
        });

        // Creating window layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Total Hours:"), hoursSpinner);
        grid.addRow(1, okBtn, cancelBtn);

        stage.setScene(new Scene(grid, 420, 160));
        stage.showAndWait();

        return result[0];
    }

    public Parent getRoot() {
        return root;
    }
}
