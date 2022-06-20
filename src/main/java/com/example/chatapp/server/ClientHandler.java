package com.example.chatapp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatServer server;
    private String nick;
    private AuthService authService;


    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.authService = authService;
            this.server = server;
            this.socket = socket;
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            readMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        while (true) {
            try {
                String message = in.readUTF();
                if (message.startsWith("/auth")) {
                    String[] split = message.split("\\p{Blank}");
                    String login = split[1];
                    String password = split[2];
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickAuth(nick)) {
                            sendMessage("This user is already authenticated!");
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast(nick, "has entered the chat");
                        server.subscribe(this);
                        break;
                    } else {
                        sendMessage("Invalid login and password");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage("/end");
        server.broadcast(nick, " left the chat!");
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
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        new Thread(() -> {
            try {
                authenticate();
                while (true) {
                    try {
                        String message = in.readUTF();
                        if ("/end".equalsIgnoreCase(message)) {
                            break;
                        }
                        server.sendMessage(nick, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                closeConnection();
            }
        }).start();
    }

    public String getNick() {
        return nick;
    }
}
