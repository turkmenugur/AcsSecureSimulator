package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.db.UnitRepository;
import com.acsteknoloji.simulator.model.Unit;
import com.acsteknoloji.simulator.service.ActivityLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Optional;

public class UserManagementController {

    @FXML
    private TableView<Unit> unitsTableView;
    @FXML
    private TableColumn<Unit, Integer> idColumn;
    @FXML
    private TableColumn<Unit, String> unitNameColumn;
    @FXML
    private TableColumn<Unit, String> roleColumn;

    @FXML
    private TextField newUnitNameField;
    @FXML
    private PasswordField newUnitPasswordField;
    @FXML
    private ComboBox<String> newUnitRoleComboBox;
    @FXML
    private Button addUnitButton;

    @FXML
    private PasswordField resetPasswordField;
    @FXML
    private Button resetPasswordButton;
    @FXML
    private Button deleteUnitButton;

    @FXML
    private Label statusLabel;

    private UnitRepository unitRepository;
    private ActivityLogger activityLogger;
    private Unit komutaUnit;
    private ObservableList<Unit> unitList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        unitNameColumn.setCellValueFactory(new PropertyValueFactory<>("unitName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        newUnitRoleComboBox.getItems().addAll("OPERATOR", "KOMUTA");
        newUnitRoleComboBox.setValue("OPERATOR");

        deleteUnitButton.setDisable(true);
        resetPasswordButton.setDisable(true);
        resetPasswordField.setDisable(true);

        unitsTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isSelected = (newSelection != null);
                    deleteUnitButton.setDisable(!isSelected);
                    resetPasswordButton.setDisable(!isSelected);
                    resetPasswordField.setDisable(!isSelected);
                });

        unitsTableView.setItems(unitList);
    }

    public void initData(UnitRepository unitRepository, ActivityLogger activityLogger, Unit komutaUnit) {
        this.unitRepository = unitRepository;
        this.activityLogger = activityLogger;
        this.komutaUnit = komutaUnit;

        // Veri enjekte edildikten sonra tabloyu doldur
        loadUnitsTable();
    }

    private void loadUnitsTable() {
        List<Unit> allUnits = unitRepository.getAllUnits();
        unitList.setAll(allUnits);
        unitsTableView.refresh();
        showStatus("Birim listesi güncellendi.", false);
    }

    @FXML
    private void onAddUnitButtonClick() {
        String unitName = newUnitNameField.getText();
        String password = newUnitPasswordField.getText();
        String role = newUnitRoleComboBox.getValue();

        if (unitName.isBlank() || password.isBlank() || role == null) {
            showStatus("Hata: Tüm alanlar doldurulmalıdır.", true);
            return;
        }

        boolean success = unitRepository.addUnit(unitName, password, role);

        if (success) {
            showStatus(unitName + " birimi başarıyla eklendi.", false);
            activityLogger.log(komutaUnit.getUnitName(), "UNIT_ADDED", "Yeni birim oluşturuldu: " + unitName);
            newUnitNameField.clear();
            newUnitPasswordField.clear();
            loadUnitsTable();
        } else {
            showStatus("Hata: " + unitName + " zaten mevcut veya veritabanı hatası.", true);
        }
    }

    @FXML
    private void onDeleteUnitButtonClick() {
        Unit selectedUnit = unitsTableView.getSelectionModel().getSelectedItem();
        if (selectedUnit == null) {
            return; // Buton zaten disable olmalı ama garanti olsun
        }

        // KKM'nin silinmesini engelle (Repository'de de kontrol var ama UI'da da olmalı)
        if ("KKM".equals(selectedUnit.getUnitName())) {
            showStatus("Hata: Ana KOMUTA birimi silinemez.", true);
            return;
        }

        // Silme onayı al
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Birim Silme Onayı");
        alert.setHeaderText(selectedUnit.getUnitName() + " birimini silmek üzeresiniz.");
        alert.setContentText("Bu işlem geri alınamaz. Emin misiniz?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Kullanıcı OK'e bastı
            boolean success = unitRepository.deleteUnit(selectedUnit.getUnitName());

            if (success) {
                showStatus(selectedUnit.getUnitName() + " birimi silindi.", false);
                activityLogger.log(komutaUnit.getUnitName(), "UNIT_DELETED", "Birim silindi: " + selectedUnit.getUnitName());
                loadUnitsTable(); // Tabloyu yenile
            } else {
                showStatus("Hata: Birim silinemedi.", true);
            }
        }
    }

    @FXML
    private void onResetPasswordButtonClick() {
        Unit selectedUnit = unitsTableView.getSelectionModel().getSelectedItem();
        String newPassword = resetPasswordField.getText();

        if (selectedUnit == null) return;

        if (newPassword.isBlank()) {
            showStatus("Hata: Yeni parola alanı boş olamaz.", true);
            return;
        }

        boolean success = unitRepository.updatePassword(selectedUnit.getUnitName(), newPassword);

        if (success) {
            showStatus(selectedUnit.getUnitName() + " biriminin parolası güncellendi.", false);
            activityLogger.log(komutaUnit.getUnitName(), "PASSWORD_RESET", "Birim parolası sıfırlandı: " + selectedUnit.getUnitName());
            resetPasswordField.clear();
        } else {
            showStatus("Hata: Parola güncellenemedi.", true);
        }
    }

    /**
     * Alt taraftaki statusLabel'i günceller.
     * @param message Gösterilecek mesaj
     * @param isError Mesaj bir hata mesajı mı (kırmızı renk)
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText("Durum: " + message);
        if (isError) {
            statusLabel.setTextFill(Color.RED);
        } else {
            statusLabel.setTextFill(Color.BLACK);
        }
    }
}
