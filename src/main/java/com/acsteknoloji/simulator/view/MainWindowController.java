package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.db.MessageRepository;
import com.acsteknoloji.simulator.db.UnitRepository;
import com.acsteknoloji.simulator.model.Message;
import com.acsteknoloji.simulator.model.MessageStatus;
import com.acsteknoloji.simulator.model.Unit;
import com.acsteknoloji.simulator.service.ActivityLogger;
import com.acsteknoloji.simulator.service.CryptographyService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
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
        this.messageRepository = new MessageRepository();
        this.cryptographyService = new CryptographyService();
        this.activityLogger = new ActivityLogger();

        commandComboBox.getItems().addAll("RAPOR VER, MEVZİYİ KORU, ÜSSE DÖN");
        System.out.println("MainWindowController FXML yüklendi, oturum bilgisi bekleniyor...");
    }

    private void updateUIForSession(){
//        Stage stage = (Stage) unitListView.getScene().getWindow();
        this.stage.setTitle("ACS Simülatör - Oturum: " + loggedInUnit.getUnitName());
        unitListView.getItems().clear();
        List<String> otherUnits = unitRepository.getOtherUnitNames(loggedInUnit.getUnitName());
        unitListView.getItems().addAll(otherUnits);
        loadMessageHistory();
    }

    /**
     * Oturum açan kullanıcının rolüne göre geçmiş mesajları veritabanından çeker ve listeler.
     */
    private void loadMessageHistory() {
        messageListView.getItems().clear();
        List<Message> history;

        // Rolü kontrol et
        if ("KOMUTA".equals(loggedInUnit.getRole())) {
            // KOMUTA ise tüm mesajları çek
            history = messageRepository.getAllMessages();
        } else {
            // OPERATOR ise sadece kendi mesajlarını çek
            history = messageRepository.getMessagesForUnit(loggedInUnit.getUnitName());
        }

        // Çekilen mesajları arayüz formatına uygun şekilde listeye ekle
        for (Message msg : history) {
            String formattedMessage = String.format("[%s] -> %s: %s",
                    msg.getSenderUnit(), msg.getReceiverUnit(), msg.getOriginalText());
            messageListView.getItems().add(formattedMessage);
        }
    }

    @FXML
    private void onSendButtonClick(){
        String receiverUnitName = unitListView.getSelectionModel().getSelectedItem();
        String messageText = messageTextField.getText();

        if (receiverUnitName == null || receiverUnitName.isEmpty()){
            // TODO: Arayüzde bir uyarı göster (örn: Alert diyalogu)
            System.out.println("HATA: Lütfen bir alıcı birim seçin.");
            return;
        }

        if (messageText == null || messageText.isBlank()){
            System.out.println("HATA: Lütfen bir mesaj yazın.");
            return;
        }

        try {
            // 2. Mesaj nesnesini oluştur ve şifrele
            Message newMessage = new Message();
            newMessage.setSenderUnit(loggedInUnit.getUnitName());
            newMessage.setReceiverUnit(receiverUnitName);
            newMessage.setOriginalText(messageText);
            newMessage.setTimestamp(LocalDateTime.now());
            newMessage.setIntegrityStatus(true); // Şimdilik varsayılan
            newMessage.setStatus(MessageStatus.GONDERILDI);

            // Kriptografi işlemlerini yap
            String encryptedText = cryptographyService.encrypt(messageText);
            String messageHash = cryptographyService.hash(messageText);
            newMessage.setEncryptedText(encryptedText);
            newMessage.setMessageHash(messageHash);
            System.out.println("Şifreleme başarılı.");

            // 3. Veritabanına kaydet
            boolean isSaved = messageRepository.saveMessage(newMessage);

            // 4. Arayüzü güncelle ve log kaydı at
            if (isSaved) {
                String formattedMessage = String.format("[%s] -> %s: %s",
                        loggedInUnit.getUnitName(), receiverUnitName, messageText);
                messageListView.getItems().add(formattedMessage);
                messageTextField.clear();

                // Aktiviteyi logla
                String logDetails = String.format("Alıcı: %s, Mesaj: '%s'", receiverUnitName, messageText);
                activityLogger.log(loggedInUnit.getUnitName(), "MESSAGE_SENT", logDetails);
            } else {
                System.out.println("HATA: Mesaj veritabanına kaydedilemedi.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Arayüzde kullanıcıya bir hata mesajı göster
            System.out.println("HATA: Mesaj gönderilirken bir istisna oluştu.");
        }
    }

}
