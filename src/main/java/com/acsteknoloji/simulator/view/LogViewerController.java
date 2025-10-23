package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.db.ActivityLogRepository;
import com.acsteknoloji.simulator.model.ActivityLog;
import com.acsteknoloji.simulator.model.Unit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogViewerController {
    @FXML
    private TextField unitFilterField;
    @FXML
    private TextField actionFilterField;
    @FXML
    private Button filterButton;
    @FXML
    private TableView<ActivityLog> logsTableView;
    @FXML
    private TableColumn<ActivityLog, Long> logIdColumn;
    @FXML
    private TableColumn<ActivityLog, LocalDateTime> logTimestampColumn;
    @FXML
    private TableColumn<ActivityLog, String> unitNameColumn;
    @FXML
    private TableColumn<ActivityLog, String> actionTypeColumn;
    @FXML
    private TableColumn<ActivityLog, String> actionDetailsColumn;
    @FXML
    private TableColumn<ActivityLog, String> ipAddressColumn;
    @FXML
    private TableColumn<ActivityLog, String> hostnameColumn;
    @FXML
    private Label statusLabel;

    // Servis ve Veri Alanları
    private ActivityLogRepository logRepository;
    private Unit komutaUnit;
    private ObservableList<ActivityLog> logList = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @FXML
    private void initialize() {
        // TableView sütunlarını ActivityLog modelindeki alanlara bağla
        logIdColumn.setCellValueFactory(new PropertyValueFactory<>("logId"));
        logTimestampColumn.setCellValueFactory(new PropertyValueFactory<>("logTimestamp"));
        unitNameColumn.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        actionDetailsColumn.setCellValueFactory(new PropertyValueFactory<>("actionDetails"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        hostnameColumn.setCellValueFactory(new PropertyValueFactory<>("hostname"));

        // Zaman damgasını (LocalDateTime) güzel formatlamak için özel CellFactory
        logTimestampColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATETIME_FORMATTER.format(item));
                }
            }
        });

        // Tabloyu ObservableList'e bağla
        logsTableView.setItems(logList);
    }

    /**
     * MainWindowController tarafından çağrılır.
     * Gerekli servisleri enjekte eder ve ilk veriyi yükler.
     */
    public void initData(ActivityLogRepository logRepository, Unit komutaUnit) {
        this.logRepository = logRepository;
        this.komutaUnit = komutaUnit;

        // Veri enjekte edilir edilmez logları yükle
        loadLogs();
    }

    /**
     * "Filtrele / Yenile" butonuna tıklandığında çalışır.
     */
    @FXML
    private void onFilterButtonClick() {
        loadLogs();
    }

    /**
     * Depodan (repository) logları çeker ve tabloyu günceller.
     * Filtre alanlarındaki metinleri kullanır.
     */
    private void loadLogs() {
        String unitFilter = unitFilterField.getText();
        String actionFilter = actionFilterField.getText();

        // Depo metodumuz zaten boş/null filtreleri yönetebiliyordu
        List<ActivityLog> allLogs = logRepository.getLogs(unitFilter, actionFilter);

        // ObservableList'i (ve dolayısıyla TableView'i) güncelle
        logList.setAll(allLogs);

        logsTableView.refresh();
        statusLabel.setText("Durum: " + allLogs.size() + " log kaydı bulundu.");
    }
}
