package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.model.Message;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

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
    @FXML
    private HBox contentBox;
    @FXML
    private FontIcon fileTypeIcon;
    @FXML
    private FontIcon downloadIcon;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private Message message;
    private MainWindowController mainWindowController;

    public void setData(Message message, String currentUnitName, MainWindowController mainWindowController) {
        this.message = message;
        this.mainWindowController = mainWindowController;

        senderLabel.setText(message.getSenderUnit());
        messageLabel.setText(message.getOriginalText());
        timestampLabel.setText(message.getTimestamp().format(TIME_FORMATTER));

        boolean isSentByMe = message.getSenderUnit().equals(currentUnitName);

        bubble.getStyleClass().removeAll("sent", "received");

        if (isSentByMe) {
            rootPane.setAlignment(Pos.CENTER_RIGHT);
            bubble.getStyleClass().add("sent");
        } else {
            rootPane.setAlignment(Pos.CENTER_LEFT);
            bubble.getStyleClass().add("received");
        }

        if ("FILE".equals(message.getMessageType())){
            fileTypeIcon.setVisible(true);
            messageLabel.setStyle("-fx-font-style: italic;");

            if (!isSentByMe){
                downloadIcon.setVisible(true);
                downloadIcon.setManaged(true);
                downloadIcon.setOnMouseClicked(event -> {
                    mainWindowController.handleFileDownload(message);
                    event.consume();
                });

            } else {
                downloadIcon.setVisible(false);
                downloadIcon.setManaged(false);
                downloadIcon.setOnMouseClicked(null);
            }
        } else {
            fileTypeIcon.setVisible(false);
            downloadIcon.setVisible(false);
            downloadIcon.setManaged(false);
            downloadIcon.setOnMouseClicked(null);
        }
    }

}
