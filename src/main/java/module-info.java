module com.javajammers.dhwanii.dhwanii {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.javajammers.dhwanii to javafx.fxml;
    exports com.javajammers.dhwanii;
}