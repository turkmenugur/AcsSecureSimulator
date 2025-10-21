package com.acsteknoloji.simulator.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class MainWindowController {

    @FXML
    private ListView<String> unitListView;

    @FXML
    private ListView<String> messageListView;

    @FXML
    private TextField messageTextField;

    @FXML
    private Button sendButton;

    @FXML
    private ComboBox<String> commandComboBox;

    @FXML
    private Button attachFileButton;

    @FXML
    private VBox detailsPane;

    @FXML
    private Label senderLabel;

    @FXML
    private Label receiverLabel;

    @FXML
    private Label timestampLabel;

    @FXML
    private void initialize(){
        unitListView.getItems().addAll("Komuta_Merkezi", "Birim_Alfa", "Birim_Bravo", "Drone_7");
        commandComboBox.getItems().addAll("RAPOR VER, MEVZİYİ KORU, ÜSSE DÖN");
    }

    @FXML
    private void onSendButtonClick(){
        String selectedUnit = unitListView.getSelectionModel().getSelectedItem();
        String messageText = messageTextField.getText();

        if (selectedUnit == null || selectedUnit.isEmpty()){
            System.out.println("HATA: Lütfen bir alıcı birim seçin.");
            return;
        }

        if (messageText == null || messageText.isBlank()){
            System.out.println("HATA: Lütfen bir mesaj yazın.");
            return;
        }

        System.out.println("'"+ selectedUnit +"' birimine gönderiliyor: '" + messageText + "'");
        messageTextField.clear();
    }

}
