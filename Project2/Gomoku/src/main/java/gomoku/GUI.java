/* This class implements the GUI for the game screen.
   It implements some of the major functions of the game, including the undo-redo, countdown, switching users, and displaying the current board and some messages.
   Remember, to play the game, you should run the Main Class.
 * */


package gomoku;

import javafx.geometry.Pos;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;


public class GUI {

    // Scene for the game screen
    private Scene scene;

    // Size for the board
    private static final int BOARD_SIZE = 20;

    // The pixel size for a single cell and the whole board.
    private static final double CELL_SIZE = 30;
    private static final double BOARD_PIXEL_SIZE = (BOARD_SIZE - 1) * CELL_SIZE;

    // Handle the game.
    private Board b;

    // Stacks for the undo-redo functions
    private Stack<Board> boardHistory =  new Stack<>();
    private Stack<Board> boardResume = new Stack<>();

    // The time left for a player to make a move
    private int timeLeft = 30;

    // Timeline for the countdown
    private Timeline timeline;

    public GUI(Stage primaryStage, Runnable resetCallback) {
        // Initialize the Board
        b = new Board(BOARD_SIZE, BOARD_SIZE);

        // Title
        Text title = new Text("GOMOKU");
        title.setStyle("-fx-font-size: 60px; -fx-font-weight: bold;");
        title.setFont(Font.font("Gloucester MT Extra Condensed"));

        // The New Game button.
        Button newGame = new Button("New Game");
        newGame.setPrefSize(100, 50);
        newGame.setAlignment(Pos.CENTER);
        newGame.setOnAction((e) -> {GUI game = new GUI(primaryStage, resetCallback);
                                                primaryStage.setScene(game.getScene());});

        // The Exit button
        Button exitGame = new Button("Exit");
        exitGame.setPrefSize(100, 50);
        exitGame.setAlignment(Pos.CENTER);
        exitGame.setOnAction(e -> resetCallback.run());

        VBox newExitGame = new VBox(20, exitGame, newGame);

        // Helper object to keep the symmetric figures
        Region empty1 = new Region();
        empty1.setPrefSize(100, 120);

        HBox titleNewExitGame = new HBox(200, empty1, title, newExitGame);
        titleNewExitGame.setAlignment(Pos.CENTER);

        // Player Informations
        VBox player1Box = createPlayerBox("Player 1");
        VBox player2Box = createPlayerBox("Player 2");

        // Player 1 plays first
        Text p1 = (Text) player1Box.getChildren().get(0);
        p1.setFill(Color.RED);

        // Countdown Area
        Text timeCountdown = new Text("Time Left");
        timeCountdown.setFill(Color.PURPLE);
        timeCountdown.setFont(Font.font("Lucida Sans", FontWeight.MEDIUM, FontPosture.ITALIC,24));
        Label countDownLabel = new Label(String.valueOf(timeLeft));
        countDownLabel.setFont(Font.font("Lucida Sans", FontWeight.MEDIUM,28));
        countDownLabel.setTextFill(Color.BLUEVIOLET);
        VBox countDownBox = new VBox(10, timeCountdown, countDownLabel);
        countDownBox.setAlignment(Pos.CENTER);

        HBox playersInfo = new HBox(50, player1Box,countDownBox, player2Box);
        playersInfo.setAlignment(Pos.CENTER);

        // Undo/Redo Buttons
        Button undoButton = new Button("Undo");
        Button redoButton = new Button("Redo");
        HBox undoRedoBox = new HBox(10, undoButton, redoButton);
        undoRedoBox.setAlignment(Pos.CENTER);

        // Status Text Field to display useful messsages
        TextField statusField = new TextField("Game is starting...");
        statusField.setEditable(false);
        statusField.setPrefSize(400, 100);
        statusField.setAlignment(Pos.CENTER);
        statusField.setFont(Font.font("Lucida Bright", FontWeight.BOLD, 20));

        // Gomoku Board area
        Canvas boardCanvas = new Canvas(BOARD_PIXEL_SIZE + CELL_SIZE * 2, BOARD_PIXEL_SIZE + CELL_SIZE * 2);
        drawBoard(boardCanvas);

        // Listen to mouse events
        boardCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleMouseClick(e, boardCanvas, statusField, player1Box, player2Box, countDownLabel));

        // Listen to button events
        undoButton.setOnAction(e -> undo(boardCanvas, player1Box, player2Box, statusField, countDownLabel));
        redoButton.setOnAction(e -> redo(boardCanvas, player1Box, player2Box, statusField, countDownLabel));

        // Layout for the game
        VBox layout = new VBox(20, titleNewExitGame, playersInfo, undoRedoBox, statusField, boardCanvas);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-spacing: 10;");
        scene = new Scene(layout, 800, 1000);

    }

    // Deal with countdown functions
    private void countdown(Label countDownLabel, VBox player1Box, VBox player2Box, TextField statusField) {
        // Terminate all the previous countdowns
        if (timeline != null) {
            timeline.stop();
        }

        // Reset the time
        timeLeft = 30;

        // Get the players text for switching players
        Text player1 = (Text) player1Box.getChildren().get(0);
        Text player2 = (Text) player2Box.getChildren().get(0);

        // Countdown
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (timeLeft > 0) {
                timeLeft--;
                countDownLabel.setText(String.valueOf(timeLeft));
            }
            else {
                // Time's up
                b.switchTurn();
                setCurrentPlayer(b.getPlayer1Turn(), player1, player2);
                statusField.setText("TIME OUT! Player has switched.");
                statusField.setStyle("-fx-text-fill: green;");
                countdown(countDownLabel, player1Box, player2Box, statusField);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // Undo function
    private void undo(Canvas canvas, VBox player1Box, VBox player2Box, TextField statusField, Label countDownLabel) {
        undoRedoHelper(true, canvas, player1Box, player2Box, statusField, boardHistory, boardResume, countDownLabel);
    }

    // Redo function
    private void redo(Canvas canvas, VBox player1Box, VBox player2Box, TextField statusField,  Label countDownLabel) {
        undoRedoHelper(false, canvas, player1Box, player2Box, statusField, boardResume, boardHistory, countDownLabel);
    }

    /* Helper method to deal with the undo redo functions.
       Basically, the idea behind the 2 functions are similar, so it can be done with a helper method with different imputs.
    * */
    private void undoRedoHelper(boolean isUndo, Canvas canvas, VBox player1Box, VBox player2Box, TextField statusField, Stack<Board> boardResume, Stack<Board> boardHistory, Label  countDownLabel) {
        // Check whether it can be undone or redone
        if (!boardResume.empty()) {
            // Save the current board to the other stack
            Board save = new Board(b);
            boardHistory.push(save);

            // Get the last board
            b = boardResume.pop();
            drawBoard(canvas);

            // Get the player texts
            Text player1 = (Text) player1Box.getChildren().get(0);
            Text player2 = (Text) player2Box.getChildren().get(0);

            setCurrentPlayer(b.getPlayer1Turn(), player1, player2);

            // Update the players' move numbers and the maximum lengths
            HBox player1MoveBox = (HBox) player1Box.getChildren().get(1);
            Text player1Move = (Text) player1MoveBox.getChildren().get(1);
            HBox player2MoveBox = (HBox) player2Box.getChildren().get(1);
            Text player2Move = (Text) player2MoveBox.getChildren().get(1);

            HBox player1LengthBox = (HBox) player1Box.getChildren().get(2);
            Text player1Length = (Text) player1LengthBox.getChildren().get(1);
            HBox player2LengthBox = (HBox) player2Box.getChildren().get(2);
            Text player2Length = (Text) player2LengthBox.getChildren().get(1);

            player1Move.setText(String.valueOf(b.player1Move()));
            player2Move.setText(String.valueOf(b.player2Move()));
            player1Length.setText(String.valueOf(b.getBlackMaxLength()));
            player2Length.setText(String.valueOf(b.getWhiteMaxLength()));

            // Re-display the text messages of the last move
            String displayText = b.moveText();
            statusField.setText((displayText));
            statusField.setStyle("-fx-text-fill: black;");

            // If it ends, terminate the countdown
            if (b.checkBlackWinningState() || b.checkWhiteWinningState()) {
                timeline.stop();
            }
            // Reset countdown
            else {
                countDownLabel.setText("30");
                countdown(countDownLabel, player1Box, player2Box, statusField);
            }
        }
        // Nothing to undo/redo
        else {
            statusField.setText(isUndo ? "Nothing to undo." : "Nothing to redo.");
            statusField.setStyle("-fx-text-fill: red;");
        }
    }

    // Helper method to create a player box, including the player text, total moves, and the maximum length of unbroken rows.
    private VBox createPlayerBox(String playerName) {
        // Player text
        Text playerLabel = new Text(playerName);
        playerLabel.setFont(Font.font("Lucida Bright", FontWeight.BOLD, 24));

        // Total move
        Text moveNumber = new Text("0");
        Text moves = new Text ("Moves:  ");
        moves.setFont(Font.font("Lucida Sans" , FontWeight.LIGHT,16));
        moveNumber.setFont(Font.font("Lucida Sans" , FontWeight.SEMI_BOLD,16));
        HBox moveNumberBox = new HBox(moves, moveNumber);
        moveNumberBox.setAlignment(Pos.CENTER);

        // Max length of unbroken rows.
        Text maxLength = new Text("Maximum Length:  ");
        maxLength.setFont(Font.font("Lucida Sans" , FontWeight.LIGHT,16));
        Text maxLengthNumber = new Text("0");
        maxLengthNumber.setFont(Font.font("Lucida Sans" , FontWeight.SEMI_BOLD,16));
        HBox maxLengthBox = new HBox(maxLength, maxLengthNumber);
        maxLengthBox.setAlignment(Pos.CENTER);

        VBox playerBox = new VBox(10, playerLabel, moveNumberBox, maxLengthBox);
        playerBox.setAlignment(Pos.CENTER);
        return playerBox;
    }

    // Helper method to draw the current state of the board.
    private void drawBoard(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Background color
        gc.setFill(Color.BEIGE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the lines of the board
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = 0; i < BOARD_SIZE; i++) {
            // Vertical lines
            gc.strokeLine((i + 1) * CELL_SIZE, CELL_SIZE, (i + 1) * CELL_SIZE, BOARD_PIXEL_SIZE + CELL_SIZE);
            // Horizontal lines
            gc.strokeLine(CELL_SIZE, (i + 1) * CELL_SIZE, BOARD_PIXEL_SIZE + CELL_SIZE, (i + 1) * CELL_SIZE);
        }

        // Draw thick borders
        gc.setLineWidth(2);
        gc.strokeRect(CELL_SIZE, CELL_SIZE, BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE);

        // Draw existing pieces
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Black: 1; White: -1;
                if (b.getBoardItem(row, col) == 1) {
                    drawPiece(gc, row, col, Color.BLACK);
                } else if (b.getBoardItem(row, col) == -1) {
                    drawPiece(gc, row, col, Color.WHITE);
                }
            }
        }
    }

    // Helper method to draw a single piece
    private void drawPiece(GraphicsContext gc, int row, int col, Color color) {
        // Calculate the intersection coordinates
        double x = col * CELL_SIZE + CELL_SIZE; // X-coordinate of the intersection
        double y = row * CELL_SIZE + CELL_SIZE; // Y-coordinate of the intersection
        double radius = CELL_SIZE / 3.0; // Radius of the piece

        // Draw the piece on the intersection
        gc.setFill(color);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    // Helper method to handle mouse events
    private void handleMouseClick(MouseEvent e, Canvas canvas, TextField statusField, VBox player1Box, VBox player2Box, Label countDownLabel) {
        // Determine the clicked intersection
        double mouseX = e.getX();
        double mouseY = e.getY();

        // Get the rows and columns in the board
        int col = (int) Math.round(mouseX / CELL_SIZE) - 1;
        int row = (int) Math.round(mouseY / CELL_SIZE) - 1;

        // Get the nodes in the player-infos
        Text player1 = (Text) player1Box.getChildren().get(0);
        Text player2 = (Text) player2Box.getChildren().get(0);

        HBox player1MoveBox = (HBox) player1Box.getChildren().get(1);
        Text player1Move = (Text) player1MoveBox.getChildren().get(1);
        HBox player2MoveBox = (HBox) player2Box.getChildren().get(1);
        Text player2Move = (Text) player2MoveBox.getChildren().get(1);

        HBox player1LengthBox = (HBox) player1Box.getChildren().get(2);
        Text player1Length = (Text) player1LengthBox.getChildren().get(1);
        HBox player2LengthBox = (HBox) player2Box.getChildren().get(2);
        Text player2Length = (Text) player2LengthBox.getChildren().get(1);

        // Terminate the countdown when game over
        if (b.isGameOver()) {
            timeline.stop();
            return;
        }

        // Check whether the click is within bounds
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            // Check whether the intersection is already occupied
            if (b.getBoardItem(row, col) == 0) {
                Board save = new Board(b);
                boardHistory.push(save);

                // Place the piece
                b.setBoardItem(row, col, b.getPlayer1Turn() ? 1 : -1, BOARD_SIZE, BOARD_SIZE);

                // Redraw the board with the new piece
                drawBoard(canvas);

                // Update the status field with the message
                String displayText = (b.getPlayer1Turn() ? "Player 1" : "Player 2") + " placed a piece at (" + (row + 1) + ", " + (col + 1) + ")";
                b.setMoveText(displayText);
                statusField.setText((displayText));
                statusField.setStyle("-fx-text-fill: black;");

                // Update the players' total moves and max lengths
                player1Move.setText(String.valueOf(b.player1Move()));
                player2Move.setText(String.valueOf(b.player2Move()));
                player1Length.setText(String.valueOf(b.getBlackMaxLength()));
                player2Length.setText(String.valueOf(b.getWhiteMaxLength()));

                // Check if Black wins
                if (b.checkBlackWinningState()) {
                    b.setGameOver(true);
                    statusField.setText("Player 1 wins!");
                    b.setMoveText("Player 1 wins!");
                    statusField.setStyle("-fx-text-fill: blue;");
                    timeline.stop();
                }

                // Check if White wins
                else if (b.checkWhiteWinningState()) {
                    b.setGameOver(true);
                    statusField.setText("Player 2 wins!");
                    b.setMoveText("Player 2 wins!");
                    statusField.setStyle("-fx-text-fill: blue;");
                    timeline.stop();
                }

                // Check for draw
                else if (b.checkDrawState()) {
                    b.setGameOver(true);
                    statusField.setText("Draw!");
                    b.setMoveText("Draw!");
                    statusField.setStyle("-fx-text-fill: blue;");
                    timeline.stop();
                }

                // Still plauing? Then switch turns.
                else {
                    b.switchTurn();
                    setCurrentPlayer(b.getPlayer1Turn(), player1, player2);
                    countDownLabel.setText("30");
                    countdown(countDownLabel, player1Box, player2Box, statusField);
                }
            }
            // Intersection is already occupied
            else {
                statusField.setText("INVALID MOVE !!!");
                statusField.setStyle("-fx-text-fill: red;");
            }
        }
        // Other moves are invalid.
        else {
            statusField.setText("INVALID MOVE !!!");
            statusField.setStyle("-fx-text-fill: red;");
        }
    }

    // Helper method to set the text of the current player to be red.
    private void setCurrentPlayer(boolean isPlayer1Turn, Text p1, Text p2) {
        if (isPlayer1Turn) {
            p1.setFill(Color.RED);
            p2.setFill(Color.BLACK);
        }
        else {
            p1.setFill(Color.BLACK);
            p2.setFill(Color.RED);
        }
    }

    // Return the scene of the current game screen
    public Scene getScene() {
        return scene;
    }
}
