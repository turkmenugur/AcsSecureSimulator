package com.acsteknoloji.simulator.view;

import com.acsteknoloji.simulator.db.UnitRepository;
import com.acsteknoloji.simulator.model.Unit;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class LoginController {
    @FXML
    private ComboBox<String> unitComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    private UnitRepository unitRepository;

    @FXML
    private void initialize() {
        this.unitRepository = new UnitRepository();
        unitComboBox.getItems().setAll(unitRepository.getAllUnitNames());
        statusLabel.setText("");
    }

    @FXML
    private void onLoginButtonClicked() {
        String selectedUnitName = unitComboBox.getValue();
        String password = passwordField.getText();

        if (selectedUnitName == null || selectedUnitName.isEmpty()) {
            statusLabel.setText("Lütfen bir birim seçin.");
            return;
        }
        if (password.isBlank()) {
            statusLabel.setText("Lütfen şifrenizi girin.");
            return;
        }

        Optional<Unit> unitOptional = unitRepository.findUnitByName(selectedUnitName);

        if (unitOptional.isPresent()) {
            Unit unit = unitOptional.get();
            if (BCrypt.checkpw(password, unit.getPasswordHash())) {
                statusLabel.setText("Giriş Başarılı!");
                System.out.println("Başarılı Giriş: " + unit.getUnitName() + ", Rol: " + unit.getRole());
            } else {
                statusLabel.setText("Hatalı şifre!");
            }
        } else {
            statusLabel.setText("Böyle bir birim bulunamadı!");
        }
    }
}
