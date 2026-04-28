import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class UpdateHours {

    // Method to show the Update Hours dialog
    public static Double showUpdateHours(Window owner, Task task) {

        // Create the stage
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Update Hours");

        Label nameLabel = new Label(task.getName());

        final double min = 0.5;
        double max = 24.0;
        if (Double.isNaN(max) || Double.isInfinite(max)) max = 2.0;
        if (max < min) max = min;

        // snap max down to nearest 0.5
        max = Math.floor(max * 2.0) / 2.0;

        double initial = task.getHours();
        if (initial < min) initial = min;
        if (initial > max) initial = max;

        Spinner<Double> hoursSpinner = new Spinner<>();
        SpinnerValueFactory.DoubleSpinnerValueFactory vf =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, 0.5);

        // handle empty input
        vf.setConverter(new StringConverter<Double>() {
            @Override public String toString(Double v) { return v == null ? "" : String.valueOf(v); }
            @Override public Double fromString(String s) {
                if (s == null) return vf.getValue();
                String t = s.trim();
                if (t.isEmpty()) return vf.getValue();
                return Double.parseDouble(t);
            }
        });

        hoursSpinner.setValueFactory(vf);
        hoursSpinner.setEditable(true);

        double finalMax = max;
        hoursSpinner.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                commitEditorText(hoursSpinner);
                double v = hoursSpinner.getValue();
                double rounded = Math.round(v * 2.0) / 2.0;
                if (rounded < min) rounded = min;
                if (rounded > finalMax) rounded = finalMax;
                hoursSpinner.getValueFactory().setValue(rounded);
            }
        });

        // Buttons for OK and Cancel
        Button okBtn = new Button("OK");
        Button cancelBtn = new Button("Cancel");

        final Double[] result = new Double[1];

        double finalMax1 = max;
        okBtn.setOnAction(e -> {
            commitEditorText(hoursSpinner);
            double hours = hoursSpinner.getValue();

            if (hours < min || hours > finalMax1) {
                alert(stage, "Hours must be between " + min + " and " + finalMax1);
                return;
            }
            double x = hours * 2.0;
            if (Math.abs(x - Math.round(x)) > 1e-9) {
                alert(stage, "Hours must be in 0.5-hour steps.");
                return;
            }

            result[0] = hours;
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

        grid.addRow(0, new Label("Task:"), nameLabel);
        grid.addRow(1, new Label("Hours:"), hoursSpinner);
        grid.addRow(2, okBtn, cancelBtn);

        stage.setScene(new Scene(grid, 420, 180));
        stage.showAndWait();

        return result[0];
    }

    // Commit the text in the spinner's editor to the spinner's value
    private static void commitEditorText(Spinner<Double> spinner) {
        if (!spinner.isEditable()) return;
        SpinnerValueFactory<Double> vf = spinner.getValueFactory();
        if (vf == null) return;
        StringConverter<Double> c = vf.getConverter();
        if (c == null) return;

        try {
            Double value = c.fromString(spinner.getEditor().getText());
            if (value != null) vf.setValue(value);
        } catch (Exception ex) {
            spinner.getEditor().setText(c.toString(vf.getValue()));
        }
    }

    // Show an alert dialog
    private static void alert(Window owner, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.initOwner(owner);
        a.showAndWait();
    }
}
