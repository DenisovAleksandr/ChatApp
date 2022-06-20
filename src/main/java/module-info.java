module com.example.chatapp {
    requires javafx.controls;
    requires javafx.fxml;



    exports com.example.chatapp.client;
    opens com.example.chatapp.client to javafx.fxml;
}