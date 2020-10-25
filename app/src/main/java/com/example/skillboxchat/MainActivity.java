package com.example.skillboxchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatWindow;
    private Button sendMessage;
    private EditText inputMessage;
    private MessageController controller;

    //TextView, выводящее количество пользователей
    private TextView numberUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatWindow = findViewById(R.id.chatWindow_mainactivity);
        sendMessage = findViewById(R.id.sendMessage_mainactivity);
        inputMessage = findViewById(R.id.inputMessage_mainactivity);

        numberUsers = findViewById(R.id.numberUsers);

        controller = new MessageController();

        controller
                .setIncomingLayout(R.layout.incoming_message)
                .setOutgoingLayout(R.layout.outgoing_message)
                .setMessageTextId(R.id.messageText)
                .setMessageTimeId(R.id.messageDate)
                .setUserNameId(R.id.userName)
                .appendTo(chatWindow, this);


        final Server server = new Server(new Consumer<Pair<String, String>>() {
            @Override
            public void accept(final Pair<String, String> pair) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        controller.addMessage(
                                new MessageController.Message(
                                        pair.second,
                                        pair.first,
                                        false)
                        );
                    }
                });
            }
        }, new Consumer<Integer>() {
                @Override
                public void accept(final Integer quantityUsers) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            numberUsers.setText(String.format(getString(R.string.numberUsers), quantityUsers));
                        }
                    });

                }
        }, new Consumer<String>() {
                @Override
                public void accept(final String newUsername) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String s = newUsername + " подключился к чату";
                            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        });
        server.connect();


        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = inputMessage.getText().toString();

                controller.addMessage(
                        new MessageController.Message(
                                messageText,
                                "Mike",
                                true)
                );
                inputMessage.setText("");
                server.sendMessage(messageText);
            }
        });
    }
}
