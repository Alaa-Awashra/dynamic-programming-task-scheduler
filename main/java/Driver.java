import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
public class Driver extends Application {

    @Override
    public void start(Stage stage) {
        GUI view = new GUI(stage);
        Scene scene = new Scene(view.getRoot(), 1200, 700);

        File cssFile = new File("C:\\Users\\Ibtisal\\OneDrive\\Desktop\\Algorithm\\Dynamic Programming\\src\\main\\java\\style.css");
        if (cssFile.exists()) {
            scene.getStylesheets().add(cssFile.toURI().toString());
        } else {
            System.out.println("CSS not found at: " + cssFile.getAbsolutePath());
        }

        stage.setTitle("Daily Tasks Scheduling");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
