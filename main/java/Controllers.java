import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controllers {

    private final Stage ownerStage;
    private final FileHandler fileService;
    private File currentFile;
    private TaskDataset dataset;
    private final BooleanProperty fileLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final ObservableList<Task> tasksView = FXCollections.observableArrayList();

    public Controllers(Stage ownerStage) {
        this.ownerStage = ownerStage;
        this.fileService = new FileHandler();
        this.dataset = null;
    }

    // Method for loading a file and updating the UI labels
    public void loadFile(Label fileLabel, Label hoursLabel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open tasks file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File f = chooser.showOpenDialog(ownerStage);
        if (f == null)
            return;

        TaskDataset loaded = fileService.load(f);
        this.currentFile = f;
        this.dataset = loaded;

        // Fill the observable list
        tasksView.clear();
        MyArrayList<Task> tasks = loaded.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            tasksView.add(tasks.get(i));
        }

        if (fileLabel != null)
            fileLabel.setText("Loaded: " + f.getName());

        if (hoursLabel != null)
            hoursLabel.setText("Total Hours: " + loaded.getTotalHoursAsDouble());

        fileLoaded.set(true);
        dirty.set(false);
    }

    // Method for saving the current dataset to file
    public void saveToFile() {
        if (!hasData())
            throw new IllegalStateException("No data loaded");
        if (currentFile == null)
            throw new IllegalStateException("No file selected");

        // Ensure dataset tasks are synchronized before saving
        syncDatasetFromView();
        fileService.save(currentFile, dataset);
        dirty.set(false);
    }

    // Generate the next unique task ID
    public int nextId() {
        if (!hasData())
            throw new IllegalStateException("No data loaded");

        int max = 0;
        for (int i = 0; i < tasksView.size(); i++) {
            int id = tasksView.get(i).getId();
            if (id > max)
                max = id;
        }
        return max + 1;
    }

    // Add a new task to both the observable list and dataset
    public void addTask(Task task) {
        if (!hasData()) throw new IllegalStateException("No data loaded");
        if (task == null) return;

        // Update both: the observable list and dataset
        tasksView.add(task);
        dataset.getTasks().add(task);

        dirty.set(true);
    }

    // Delete a task by its ID from both the observable list and dataset
    public void deleteTaskById(int id) {
        if (!hasData()) throw new IllegalStateException("No data loaded");

        // Remove from observable list first
        for (int i = 0; i < tasksView.size(); i++) {
            if (tasksView.get(i).getId() == id) {
                tasksView.remove(i);
                break;
            }
        }

        // Remove from dataset
        MyArrayList<Task> tasks = dataset.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == id) {
                tasks.removeAt(i);
                dirty.set(true);
                return;
            }
        }

        throw new IllegalArgumentException("Task id not found: " + id);
    }

    // Update the hours of a specific task by its ID
    public void updateTaskHours(int id, double newHours) {
        if (!hasData()) throw new IllegalStateException("No data loaded");

        double max = dataset.getTotalHoursAsDouble();
        int units = FileHandler.toUnits(newHours);
        double normalized = FileHandler.fromUnits(units);

        // enforce 0.5 steps
        if (Math.abs(normalized - newHours) > 1e-9) {
            throw new IllegalArgumentException("Hours must be in 0.5-hour steps.");
        }

        if (newHours < 0.5 || newHours > max) {
            throw new IllegalArgumentException("Hours must be between 0.5 and " + max);
        }

        // Update dataset
        MyArrayList<Task> tasks = dataset.getTasks();
        Task target = null;
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.getId() == id) {
                t.setHours(newHours);
                target = t;
                break;
            }
        }
        if (target == null) throw new IllegalArgumentException("Task id not found: " + id);

        // visible update in TableView effectively replaces the Task object
        for (int i = 0; i < tasksView.size(); i++) {
            if (tasksView.get(i).getId() == id) {
                tasksView.set(i, target);
                break;
            }
        }

        dirty.set(true);
    }

    // Update the total available hours in the dataset and UI label
    public void updateTotalHours(double newTotalHours, Label hoursLabel) {
        if (!hasData()) throw new IllegalStateException("No data loaded");

        int units = FileHandler.toUnits(newTotalHours);
        double normalized = FileHandler.fromUnits(units);

        if (normalized < 0.5) {
            throw new IllegalArgumentException("Total hours must be at least 0.5");
        }

        dataset.setTotalHoursUnits(units);
        dirty.set(true);

        if (hoursLabel != null) {
            hoursLabel.setText("Total Hours: " + dataset.getTotalHoursAsDouble());
        }
    }


    // Keeps dataset tasks exactly equal to tasksView
    private void syncDatasetFromView() {
        MyArrayList<Task> tasks = dataset.getTasks();

        // clear MyArrayList by removing from the end
        while (tasks.size() > 0) {
            tasks.removeAt(tasks.size() - 1);
        }

        for (int i = 0; i < tasksView.size(); i++) {
            tasks.add(tasksView.get(i));
        }
    }

    public BooleanProperty fileLoadedProperty() {
        return fileLoaded;
    }
    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public boolean hasData() {
        return dataset != null;
    }
    public TaskDataset getDataset() {
        return dataset;
    }
    public File getCurrentFile() {
        return currentFile;
    }
    public ObservableList<Task> getTasksView() {
        return tasksView;
    }
    public Stage getOwnerStage() {
        return ownerStage;
    }
}
