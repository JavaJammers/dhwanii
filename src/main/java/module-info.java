module com.javajammers.dhwanii.dhwanii {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires javafx.media;

    opens com.javajammers.dhwanii to javafx.fxml;
    exports com.javajammers.dhwanii;
}