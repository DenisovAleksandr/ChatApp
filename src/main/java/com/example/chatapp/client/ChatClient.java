package com.example.chatapp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private ChatController controller;


    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void OpenConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        readMessages();
    }

    private void waitAuth() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith("/authok")) {
                String[] split = message.split("\\p{Blank}");
                String nick = split[1];
                controller.setAuth(true);
                controller.addMessage("You are logged in under the nickname " + nick);
                break;
            }
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() {
        new Thread(() -> {
            try {
                waitAuth();
                while (true) {
                    String message = in.readUTF();
                    if ("/end".equalsIgnoreCase(message)) {
                        controller.setAuth(false);
                        break;
                    }
                    controller.addMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();

            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
