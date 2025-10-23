module com.acsteknoloji.simulator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires static lombok;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;
    requires org.xerial.sqlitejdbc;

    opens com.acsteknoloji.simulator to javafx.fxml;
    exports com.acsteknoloji.simulator;
    exports com.acsteknoloji.simulator.model;
    exports com.acsteknoloji.simulator.view;

    opens com.acsteknoloji.simulator.view to javafx.fxml;

}