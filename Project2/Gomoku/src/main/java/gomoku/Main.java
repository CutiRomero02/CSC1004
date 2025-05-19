/* The Main Class for the Gomoku Game.
*  You may play the game by running this Class.
*  This class also launches the login screen for the game.
* */


package gomoku;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {
        // The stage for the login screen
        Stage primaryStage;

    // Launch the login screen
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Initialize the login interface.
        initializeScreen();
    }

    // Initialize the login screen
    private void initializeScreen() {
        // Title
        Text title = new Text("GOMOKU");
        title.setStyle("-fx-font-size: 70px; -fx-font-weight: bold;");
        title.setFont(Font.font("Gloucester MT Extra Condensed"));

        // The button to start the game
        Button playButton = new Button("Play");
        playButton.setPrefSize(200, 50);
        playButton.setFont(Font.font("Lucida Sans", 28));

        // Layout the nodes
        VBox layout = new VBox(20, title, playButton);
        layout.setStyle("-fx-alignment: center; -fx-spacing: 20;");
        layout.setPrefSize(400, 300);

        // Button Actions
        playButton.setOnAction(e -> showGameScreen(primaryStage));

        // Scene
        Scene scene = new Scene(layout);
        primaryStage.setTitle("GOMOKU");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showGameScreen(Stage primaryStage) {
        // Transition to the game screen
        GUI game = new GUI(primaryStage, this::initializeScreen);
        primaryStage.setScene(game.getScene());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
