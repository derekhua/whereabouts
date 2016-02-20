package com.example.derek.whereabouts;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.appdatasearch.GetRecentContextCall;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChatRoomActivityFragment extends ListFragment {

    final ArrayList<String> list = new ArrayList<>();

    public static Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://ec2-54-165-233-14.compute-1.amazonaws.com:3000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    Emitter.Listener connectEmitter;
    Emitter.Listener disconnectEmitter;
    Emitter.Listener updateListener;
    Emitter.Listener chatListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
        setListAdapter(adapter);
        final EditText input = (EditText) getActivity().findViewById(R.id.editText);
        final Button button = (Button) getActivity().findViewById(R.id.sendButton);
        final String username = getActivity().getIntent().getStringExtra("USERNAME");
        final String room = getActivity().getIntent().getStringExtra("ROOM_NAME");

        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
            data.put("room", room);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        mSocket.emit("subscribe", data);


        connectEmitter = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("ChatRoomActivity: ", "SOCKETLOG: socket connected");
            }
        };

        disconnectEmitter = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("ChatRoomActivity: ", "SOCKETLOG: socket disconnected");
            }
        };

        updateListener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                
            }
        };

        chatListener = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json = ((JSONObject) args[0]);
                        try {
                            list.add(json.get("username") + ": " + json.get("text"));
                            adapter.notifyDataSetChanged();
                            getListView().setSelection(list.size() - 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        mSocket.on(Socket.EVENT_CONNECT, connectEmitter)
                .on(Socket.EVENT_DISCONNECT, disconnectEmitter)
                .on("update", updateListener)
                .on("chat", chatListener);
        mSocket.connect();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = input.getText().toString();
                JSONObject data = new JSONObject();
                try {
                    data.put("username", username);
                    data.put("text", text);
                    data.put("room", room);

                    if(!text.trim().equals("")) {
                        mSocket.emit("chat", data);
                        list.add(username + ": " + text.trim());
                        adapter.notifyDataSetChanged();
                        getListView().setSelection(list.size() - 1);
                        input.setText("");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, connectEmitter);
        mSocket.off(Socket.EVENT_DISCONNECT, disconnectEmitter);
        mSocket.off("update", updateListener);
        mSocket.off("chat", chatListener);
        Log.d("ChatRoomActivity: ", "SOCKETLOG: Socket off");
    }

}
