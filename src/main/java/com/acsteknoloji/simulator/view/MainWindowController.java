package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.db.MessageRepository;
import com.acsteknoloji.simulator.db.UnitRepository;
import com.acsteknoloji.simulator.model.Message;
import com.acsteknoloji.simulator.model.MessageStatus;
import com.acsteknoloji.simulator.model.Unit;
import com.acsteknoloji.simulator.service.ActivityLogger;
import com.acsteknoloji.simulator.service.CryptographyService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainWindowController {

    private Unit loggedInUnit;
    private Stage stage;
    private UnitRepository unitRepository;
    private MessageRepository messageRepository;
    private CryptographyService cryptographyService;
    private ActivityLogger activityLogger;

    @FXML
    private ListView<String> unitListView;

    @FXML
    private ListView<Message> messageListView;

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
    private Label detailsTitleLabel;
    @FXML
    private Label detailsSenderLabel;
    @FXML
    private Label detailsReceiverLabel;
    @FXML
    private Label detailsTimestampLabel;
    @FXML
    private Label detailsStatusLabel;
    @FXML
    private Label detailsIntegrityLabel;
    @FXML
    private TextArea detailsEncryptedTextArea;
    @FXML
    private TextArea detailsHashTextArea;

    public void initSession(Unit unit, Stage stage){
        this.loggedInUnit = unit;
        this.stage = stage;
        System.out.println("Ana pencere oturumu başlatıldı. Kullanıcı: " + loggedInUnit.getUnitName() + ", Rol: " + loggedInUnit.getRole());
        updateUIForSession();
        setupMessageListViewCellFactory();
        loadMessageHistory();
    }

    @FXML
    private void initialize(){
        this.unitRepository = new UnitRepository();
        this.messageRepository = new MessageRepository();
        this.cryptographyService = new CryptographyService();
        this.activityLogger = new ActivityLogger();

        commandComboBox.getItems().addAll("RAPOR VER, MEVZİYİ KORU, ÜSSE DÖN");
        System.out.println("MainWindowController FXML yüklendi, oturum bilgisi bekleniyor...");

        messageListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateDetailsPane(newValue)
        );

        clearDetailsPane();
    }

    /**
     * messageListView'den bir mesaj seçildiğinde sağdaki detay panelini günceller.
     * @param message Seçilen Mesaj nesnesi (veya seçim yoksa null)
     */
    private void updateDetailsPane(Message message){
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss");

        if (message != null){
            detailsPane.setVisible(true);
            detailsTitleLabel.setText("Mesaj Detayları (ID: " + message.getId() + ")");
            detailsSenderLabel.setText(message.getSenderUnit());
            detailsReceiverLabel.setText(message.getReceiverUnit());
            detailsTimestampLabel.setText(message.getTimestamp().format(fullFormatter));
            detailsStatusLabel.setText(message.getStatus().name());

            String integrityText = message.isIntegrityStatus() ? "Doğrulandı" : "Doğrulanamadı / Bekleniyor";
            detailsIntegrityLabel.setText(integrityText);

            detailsEncryptedTextArea.setText(message.getEncryptedText());
            detailsHashTextArea.setText(message.getMessageHash());
        } else {
            clearDetailsPane();
        }
    }

    /**
     * Detay panelindeki tüm bilgileri varsayılan "N/A" (Bilgi Yok) durumuna getirir.
     */
    private void clearDetailsPane(){
        detailsTitleLabel.setText("Mesaj Detayları");
        detailsSenderLabel.setText("N/A");
        detailsReceiverLabel.setText("N/A");
        detailsTimestampLabel.setText("N/A");
        detailsStatusLabel.setText("N/A");
        detailsIntegrityLabel.setText("N/A");
        detailsEncryptedTextArea.clear();
        detailsHashTextArea.clear();
    }

    private void updateUIForSession(){
//        Stage stage = (Stage) unitListView.getScene().getWindow();
        this.stage.setTitle("ACS Simülatör - Oturum: " + loggedInUnit.getUnitName());
        unitListView.getItems().clear();
        List<String> otherUnits = unitRepository.getOtherUnitNames(loggedInUnit.getUnitName());
        unitListView.getItems().addAll(otherUnits);
        //loadMessageHistory();
    }

    /**
     * Mesaj listesi için özel CellFactory'i ayarlar.
     * Bu fabrika, her Message nesnesi için MessageCell.fxml'i yükler.
     */
    private void setupMessageListViewCellFactory(){
        messageListView.setCellFactory(lv -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/acsteknoloji/simulator/view/MessageCell.fxml"));
                        HBox root = loader.load();
                        MessageCellController controller = loader.getController();

                        // Controller'a mesajı ve KİMİN oturum açtığını söylüyoruz
                        controller.setData(message, loggedInUnit.getUnitName());

                        setGraphic(root);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setText("Mesaj yüklenemedi: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Oturum açan kullanıcının rolüne göre geçmiş mesajları veritabanından çeker ve listeler.
     */
    private void loadMessageHistory() {
        messageListView.getItems().clear();
        List<Message> history;

        if ("KOMUTA".equals(loggedInUnit.getRole())) {
            history = messageRepository.getAllMessages();
        } else {
            history = messageRepository.getMessagesForUnit(loggedInUnit.getUnitName());
        }

        messageListView.getItems().addAll(history);
    }

    @FXML
    private void onSendButtonClick(){
        String receiverUnitName = unitListView.getSelectionModel().getSelectedItem();
        String messageText = messageTextField.getText();

        if (receiverUnitName == null || receiverUnitName.isEmpty()){
            showErrorNotification("Gönderim Hatası", "Lütfen bir alıcı birim seçin.");
            return;
        }

        if (messageText == null || messageText.isBlank()){
            showErrorNotification("Gönderim Hatası", "Lütfen bir mesaj girin.");
            return;
        }

        try {
            Message newMessage = new Message();
            newMessage.setSenderUnit(loggedInUnit.getUnitName());
            newMessage.setReceiverUnit(receiverUnitName);
            newMessage.setOriginalText(messageText);
            newMessage.setTimestamp(LocalDateTime.now());
            newMessage.setIntegrityStatus(true);
            newMessage.setStatus(MessageStatus.GONDERILDI);

            String encryptedText = cryptographyService.encrypt(messageText);
            String messageHash = cryptographyService.hash(messageText);
            newMessage.setEncryptedText(encryptedText);
            newMessage.setMessageHash(messageHash);

            boolean isSaved = messageRepository.saveMessage(newMessage);

            if (isSaved) {
                messageListView.getItems().add(newMessage);
                messageListView.scrollTo(messageListView.getItems().size() - 1);

                messageTextField.clear();

                String logDetails = String.format("Alıcı: %s, Mesaj: '%s'", receiverUnitName, messageText);
                activityLogger.log(loggedInUnit.getUnitName(), "MESSAGE_SENT", logDetails);
            } else {
                showErrorNotification("Veritabanı Hatası", "Mesaj veritabanına kaydedilemedi");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorNotification("Kritik Hata", "Mesaj gönderilirken bir hata oluştu.");
        }
    }

    /**
     * Ekranda sağ üst köşede bir hata bildirimi gösterir.
     * @param title Bildirim başlığı
     * @param text Bildirim metni
     */
    private void showErrorNotification(String title, String text){
        Notifications.create()
                .title(title)
                .text(text)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(5))
                .owner(stage)
                .showError();
    }

}
