package com.example.chatapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private final List<ClientHandler> clients;

    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new InMemoryAuthService()) {
            while (true) {
                System.out.println("Wait connection...");
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Client is connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String nick, String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(nick + ": " + message);
        }
    }

    public void privateMassage(String nickWhomToSend, String nick, String message) {
        for (ClientHandler client : clients) {
            if (client.getNick().equalsIgnoreCase(nick)) {
                client.sendMessage("PM for " + nick + ": " + message);
            }
        }
        for (ClientHandler client : clients) {
            if (client.getNick().equalsIgnoreCase(nickWhomToSend)) {
                client.sendMessage("PM from "+nick+": "+message);
                break;
            }
            for (ClientHandler yourself : clients) {
                if (yourself.getNick().equalsIgnoreCase(nick)) {
                    yourself.sendMessage("User with nickname"+nick+" is not connected to the chat");
                }
            }
        }
    }

    public void sendMessage(String nick, String message) {
        if (message.startsWith("/w")) {
            String[] split = message.split("\\p{Blank}");
            String nickWhomToSend = split[1];
            String privMessage = "";
            for (int i = 2; i < split.length; i++) {
                privMessage = privMessage + " " + split[i];
            }
            privateMassage(nickWhomToSend, nick ,": " + privMessage);
        } else {
            broadcast(nick,message);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public boolean isNickAuth(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equalsIgnoreCase(client.getNick())) {
                return true;
            }
        }
        return false;
    }
}
