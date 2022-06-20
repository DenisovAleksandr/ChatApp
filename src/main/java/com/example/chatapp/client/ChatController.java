package com.example.chatapp.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class ChatController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    private VBox messageBox;
    @FXML
    private VBox authBox;
    @FXML
    private TextField userMessage;
    @FXML
    private TextArea historyMessage;

    private  ChatClient client;

    public ChatController() {
        this.client = new ChatClient(this);
    }

    private void showNotification() {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Connection failed! \n",
                new ButtonType("Try again", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        alert.setTitle("Connection error");
        Optional<ButtonType> answer = alert.showAndWait();
        boolean isExit = answer.map(select->select.getButtonData().isCancelButton()).orElse(false);
        if(isExit){
            System.exit(0);
        }
    }

    public void sendMessageButtonClick(ActionEvent actionEvent) {
        String message = userMessage.getText();
        if (message.isBlank()){
            return;
        }
        client.sendMessage(message);
        userMessage.clear();
        userMessage.requestFocus();
    }

    public void addMessage(String message) {
        historyMessage.appendText(message+"\n");
    }

    public void signinBtnClick(ActionEvent actionEvent) {
        while (true){
            try {
                client.OpenConnection();
                break;
            }catch (IOException e){
                showNotification();
            }
        }
        client.sendMessage("/auth "+loginField.getText()+" "+passField.getText());
    }

    public void setAuth (boolean success){
        authBox.setVisible(!success);
        messageBox.setVisible(success);
    }
}