package com.example.skillboxchat;

import android.util.Log;
import android.util.Pair;

import androidx.core.util.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private WebSocketClient client;
    private Map<Long, String> names = new ConcurrentHashMap<>();

    private Consumer<Pair<String, String>> onMessageReceived; //Коллбэки

    //onUserNumbersReceived хранит количество пользователей в сети
    private Consumer<Integer> onUserNumbersReceived;

    //onUserNumbersReceived хранит имя пользователя, подключившегося к чату
    private Consumer<String> newUserReceived;

    public Server(Consumer<Pair<String, String>> onMessageReceived, Consumer<Integer> onUserNumbersReceived, Consumer<String> newUser) {
        this.onMessageReceived = onMessageReceived;
        this.onUserNumbersReceived = onUserNumbersReceived;
        this.newUserReceived = newUser;
    }

    public void connect(){

        URI address;

        try {
            address = new URI("ws://35.214.1.221:8881");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        client = new WebSocketClient(address) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("SERVER", "Connected to server");
                sendName("Mike");
            }

            @Override
            public void onMessage(String json) {
                Log.i("SERVER", "Got json from server: " + json);
                int type = Protocol.getType(json);

                switch (type){
                    case (Protocol.USER_STATUS):
                        updateStatus(Protocol.unpackStatus(json));
                        displayUsersNumbers(names);
                        displayNewUser(Protocol.unpackUsername(json), Protocol.unpackStatus(json).isConnected());
                        break;
                    case (Protocol.MESSAGE):
                        displayIncoming(Protocol.unpackMessage(json));
                        break;
                    case (Protocol.USER_NAME):
                        break;
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("SERVER", "Connection closed");
            }

            @Override
            public void onError(Exception ex) {
                Log.i("SERVER", "ERROR: " + ex.getMessage());
            }
        };
        client.connect();
    }

    public void sendName(String name){
        Protocol.UserName userName = new Protocol.UserName(name);

        if(client != null && client.isOpen()){
            client.send(Protocol.packName(userName));

        }
    }

    public void sendMessage(String text){
        Protocol.Message mess = new Protocol.Message(text);

        if(client != null && client.isOpen()){
            client.send(Protocol.packMessage(mess));
        }
    }

    private void updateStatus(Protocol.UserStatus status){
        //Запоминаем статус пользователя при подключении и удаляем при отключении

        Protocol.User user = status.getUser();
        if(status.isConnected()){
            names.put(user.getId(), user.getName());
        } else {
            names.remove(user.getId());
        }
    }

    private void displayIncoming(Protocol.Message message){
        String name = names.get(message.getSender());
        if(name == null){
            name = "Unnamed";
        }

        //отправляем в MainActivity пришедшее сообщение
        onMessageReceived.accept(new Pair<>(name, message.getEncodedText()));
    }

    //метод отправляет в MainActivity количество активных пользователей
    private void displayUsersNumbers(Map names){
        int userNumbers = names.size();

        //добавляем в Consumer onUserNumbersReceived количество активных пользователей
        onUserNumbersReceived.accept(userNumbers);
    }

    //метод отправляет в MainActivity имя подключившегося пользователя
    private void displayNewUser(String name, Boolean connected){
        //Если пользователь подключился, добавляем его имя в Consumer newUserReceived
        if(connected){
            newUserReceived.accept(name);
        }
    }
}

