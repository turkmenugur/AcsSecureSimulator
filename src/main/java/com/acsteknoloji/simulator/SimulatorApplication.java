package com.acsteknoloji.simulator;

import com.acsteknoloji.simulator.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SimulatorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, SQLException {
        DatabaseManager.initializeDatabase();
        DatabaseManager.seedInitialData();

//        FXMLLoader fxmlLoader = new FXMLLoader(SimulatorApplication.class.getResource("/com/acsteknoloji/simulator/view/MainWindow.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
//        stage.setTitle("ACS Teknoloji Güvenli Haberleşme Simülatörü");
//        stage.setScene(scene);
//        stage.show();

        FXMLLoader fxmlLoader = new FXMLLoader(SimulatorApplication.class.getResource("/com/acsteknoloji/simulator/view/LoginWindow.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 400, 350);
        stage.setTitle("ACS Simülatör - Giriş");
        stage.setScene(scene);
        stage.show();
    }

}
