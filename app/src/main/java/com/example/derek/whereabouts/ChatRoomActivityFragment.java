package com.example.derek.whereabouts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChatRoomActivityFragment extends ListFragment {

    final ArrayList<String> list = new ArrayList<String>();
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
            public void call(Object... args) {
                JSONObject json = ((JSONObject) args[0]);
                try {
                    list.add(json.getString("username") + ": " + json.getString("text"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
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
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                mSocket.emit("chat", data);
                if(text != null && !text.equals("")) {
                    list.add(username + ": " + text);
                    adapter.notifyDataSetChanged();
                    setSelection(list.size() - 1);
                    input.setText("");
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
