import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AddTask {

    public static Task show(Window owner, int suggestedId) {

        // Create the stage
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Add Task");

        // Create UI elements
        TextField nameField = new TextField();
        TextField valueField = new TextField();

        Spinner<Double> hoursSpinner = new Spinner<>();
        hoursSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 24, 0.5, 0.5));
        hoursSpinner.setEditable(true);

        // If user types, we round to nearest 0.5 on focus lost
        hoursSpinner.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                Double v = hoursSpinner.getValue();
                double rounded = Math.round(v * 2.0) / 2.0;
                if (rounded < 0.5) rounded = 0.5;
                if (rounded > 2.0) rounded = 2.0;
                hoursSpinner.getValueFactory().setValue(rounded);
            }
        });

        // Buttons
        Button addBtn = new Button("Add");
        Button cancelBtn = new Button("Cancel");

        final Task[] result = new Task[1];

        addBtn.setOnAction(e -> {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            if (name.isEmpty()) {
                alert(stage, "Task name is required.");
                return;
            }
            if (name.contains(",")) {
                alert(stage, "Task name must not contain a comma.");
                return;
            }

            int value;
            try {
                value = Integer.parseInt(valueField.getText().trim());
                if( value < 0 ) {
                    alert(stage, "Value must be a non-negative integer.");
                    return;
                }
            } catch (Exception ex) {
                alert(stage, "Value must be an integer.");
                return;
            }

            double hours = hoursSpinner.getValue();

            if (hours < 0.5 || hours > 24.0) {
                alert(stage, "Hours must be between 0.5 and 24.0 (inclusive).");
                return;
            }
            // must be in 0.5 steps
            double scaled = hours * 2.0;
            long rounded = Math.round(scaled);
            if (Math.abs(scaled - rounded) > 1e-9) {  // rejects 0. somthing other than .0 or .5
                alert(stage, "Hours must be in steps of 0.5 (0.5, 1.0, 1.5, ... 24.0).");
                return;
            }

            result[0] = new Task(suggestedId, name, hours, value);
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

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Hours:"), hoursSpinner);
        grid.addRow(2, new Label("Value:"), valueField);
        grid.addRow(3, addBtn, cancelBtn);

        stage.setScene(new Scene(grid, 420, 220));
        stage.showAndWait();

        return result[0];
    }

    // Simple alert dialog
    private static void alert(Window owner, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.initOwner(owner);
        a.showAndWait();
    }
}
