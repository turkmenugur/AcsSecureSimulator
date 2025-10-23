package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.db.MessageRepository;
import com.acsteknoloji.simulator.db.UnitRepository;
import com.acsteknoloji.simulator.model.Message;
import com.acsteknoloji.simulator.model.MessageStatus;
import com.acsteknoloji.simulator.model.Unit;
import com.acsteknoloji.simulator.service.ActivityLogger;
import com.acsteknoloji.simulator.service.CryptographyService;
import com.acsteknoloji.simulator.service.FileService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.File;
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
    private FileService fileService;

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
        this.fileService = new FileService();

        commandComboBox.getItems().addAll("RAPOR VER, MEVZİYİ KORU, ÜSSE DÖN");
        System.out.println("MainWindowController FXML yüklendi, oturum bilgisi bekleniyor...");

        messageListView.setOnMouseClicked(event -> {
            Message selectedMessage = messageListView.getSelectionModel().getSelectedItem();
            if (selectedMessage == null){
                return;
            }

            updateDetailsPane(selectedMessage);

            if ("FILE".equals(selectedMessage.getMessageType()) &&
            !selectedMessage.getSenderUnit().equals(loggedInUnit.getUnitName())){
                handleFileDownload(selectedMessage);
            }
        });

//        messageListView.getSelectionModel().selectedItemProperty().addListener(
//                (observable, oldValue, newValue) -> updateDetailsPane(newValue)
//        );

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
     * "Ataş" butonuna tıklandığında tetiklenir.
     * Kullanıcıya bir dosya seçtirir ve bu dosyayı şifreleyip gönderme işlemini başlatır.
     */
    @FXML
    private void onAttachFileButtonClick(){
        String receiverUnitName = unitListView.getSelectionModel().getSelectedItem();
        if (receiverUnitName == null || receiverUnitName.isEmpty()){
            showErrorNotification("Dosya Gönderimi Hatası", "Lütfen bir alıcı birim seçin.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Gönderilecek Dosyayı Seçin");
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null){
            System.out.println("Dosya Seçildi: " + selectedFile.getName());

            final String finalReceiver = receiverUnitName;

            Task<Message> fileProcessTask = new Task<Message>() {
                @Override
                protected Message call() throws Exception {
                    updateMessage("Dosya Okunuyor: " + selectedFile.getName());
                    byte[] originalBytes = fileService.readFileBytes(selectedFile);

                    updateMessage("Dosya hash'leniyor...");
                    String originalHash = cryptographyService.hash(originalBytes);

                    updateMessage("Dosya şifreleniyor...");
                    byte[] encryptedBytes = cryptographyService.encrypt(originalBytes);

                    updateMessage("Şifreli dosya diske kaydediliyor...");
                    String savedFilePath = fileService.saveEncryptedFile(encryptedBytes);

                    updateMessage("Veritabanı mesajı oluşturuluyor...");
                    Message fileMessage = new Message();
                    fileMessage.setSenderUnit(loggedInUnit.getUnitName());
                    fileMessage.setReceiverUnit(finalReceiver);
                    fileMessage.setOriginalText(selectedFile.getName());
                    fileMessage.setMessageType("FILE");
                    fileMessage.setFilePath(savedFilePath);
                    fileMessage.setEncryptedText(null);
                    fileMessage.setMessageHash(originalHash);
                    fileMessage.setTimestamp(LocalDateTime.now());
                    fileMessage.setStatus(MessageStatus.GONDERILDI);
                    fileMessage.setIntegrityStatus(true);

                    return fileMessage;

                }
            };

            fileProcessTask.setOnSucceeded(event -> {
                Message sentMessage = fileProcessTask.getValue();

                boolean isSaved = messageRepository.saveMessage(sentMessage);

                if (isSaved){
                    messageListView.getItems().add(sentMessage);
                    messageListView.scrollTo(messageListView.getItems().size() - 1);

                    String logDetails = String.format("Alıcı: %s, Dosya: '%s'",
                            sentMessage.getReceiverUnit(), sentMessage.getOriginalText());
                    activityLogger.log(loggedInUnit.getUnitName(), "FILE_SENT", logDetails);
                    System.out.println("Dosya mesajı başarıyla gönderildi: " + sentMessage.getOriginalText());
                }else{
                    showErrorNotification("Veritabanı Hatası", "Dosya mesajı veritabanına kaydedilemedi.");
                }
            });

            fileProcessTask.setOnFailed(event -> {
                Throwable ex = fileProcessTask.getException();
                ex.printStackTrace();
                showErrorNotification("Dosya İşleme Hatası", "Dosya gönderilirken bir hata oluştu: " + ex.getMessage());
            });

            new Thread(fileProcessTask).start();
        }

    }

    /**
     * Kullanıcı bir 'FILE' mesajına tıkladığında tetiklenir.
     * Dosyayı indirme, şifre çözme ve kaydetme sürecini yönetir.
     * @param message İndirilecek dosya mesajı
     */
    private void handleFileDownload(Message message){
        System.out.println("İndirme talebi alındı: " + message.getOriginalText());

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Dosyayı Kaydet");

        saveChooser.setInitialFileName(message.getOriginalText());
        File targetFile = saveChooser.showOpenDialog(stage);

        if (targetFile == null){
            System.out.println("Dosya kaydetme işlemi iptal edildi");
            return;
        }

        Task<Boolean> downloadTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Şifreli  dosya okunuyor...");
                byte[] encryptedBytes = fileService.readEncryptedFile(message.getFilePath());

                updateMessage("Dosya şifresi çözülüyor...");
                byte[] decryptedBytes = cryptographyService.decrypt(encryptedBytes);

                updateMessage("Bütünlük kontrolü yapılıyor (Hash)...");
                String decryptedHash = cryptographyService.hash(decryptedBytes);

                String originalHash = message.getMessageHash();

                if (!decryptedHash.equals(originalHash)){
                    throw new SecurityException("BÜTÜNLÜK HATASI! Hash değerleri uyuşmuyor. Dosya indirilmedi.");
                }

                updateMessage("Dosya Kaydediliyor...");
                fileService.saveDecryptedFile(decryptedBytes, targetFile);

                return true;
            }
        };

        downloadTask.setOnSucceeded(event -> {
            Notifications.create()
                    .title("İndirme Başarılı")
                    .text(message.getOriginalText() + " dosyası başarıyla indirildi ve doğrulandı.")
                    .position(Pos.TOP_RIGHT)
                    .owner(stage)
                    .showInformation();
        });

        downloadTask.setOnFailed(event -> {
            Throwable ex = downloadTask.getException();
            ex.printStackTrace();
            showErrorNotification("İndirme Hatası", "Dosya indirilemedi: " + ex.getMessage());
        });

        new Thread(downloadTask).start();
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
