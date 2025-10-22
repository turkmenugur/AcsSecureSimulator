package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.model.Message;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class MessageCellController {

    @FXML
    private HBox rootPane;
    @FXML
    private VBox bubble;
    @FXML
    private Label senderLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label timestampLabel;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void setData(Message message, String currentUnitName) {
        senderLabel.setText(message.getSenderUnit());
        messageLabel.setText(message.getOriginalText());
        timestampLabel.setText(message.getTimestamp().format(TIME_FORMATTER));


        if (message.getSenderUnit().equals(currentUnitName)) {
            rootPane.setAlignment(Pos.CENTER_RIGHT);
            bubble.getStyleClass().add("sent");
        } else {
            rootPane.setAlignment(Pos.CENTER_LEFT);
            bubble.getStyleClass().add("received");
        }
    }

}
