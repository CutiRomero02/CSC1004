module gomoku {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;


    opens gomoku to javafx.fxml;
    exports gomoku;
}