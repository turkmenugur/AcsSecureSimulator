package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.db.UnitRepository;
import com.acsteknoloji.simulator.model.Unit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MainWindowController {

    private Unit loggedInUnit;
    private Stage stage;
    private UnitRepository unitRepository;

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

    public void initSession(Unit unit, Stage stage){
        this.loggedInUnit = unit;
        this.stage = stage;
        System.out.println("Ana pencere oturumu başlatıldı. Kullanıcı: " + loggedInUnit.getUnitName() + ", Rol: " + loggedInUnit.getRole());
        updateUIForSession();
    }

    @FXML
    private void initialize(){
        this.unitRepository = new UnitRepository();
        commandComboBox.getItems().addAll("RAPOR VER, MEVZİYİ KORU, ÜSSE DÖN");
        System.out.println("MainWindowController FXML yüklendi, oturum bilgisi bekleniyor...");
    }

    private void updateUIForSession(){
//        Stage stage = (Stage) unitListView.getScene().getWindow();
        this.stage.setTitle("ACS Simülatör - Oturum: " + loggedInUnit.getUnitName());
        unitListView.getItems().clear();
        List<String> otherUnits = unitRepository.getOtherUnitNames(loggedInUnit.getUnitName());
        unitListView.getItems().addAll(otherUnits);
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
