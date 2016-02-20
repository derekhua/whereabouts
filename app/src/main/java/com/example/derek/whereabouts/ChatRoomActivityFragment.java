package com.example.derek.whereabouts;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

    final ArrayList<Message> messages = new ArrayList<Message>();

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
        final ArrayAdapter adapter = new MessageAdapter(getContext());
        setListAdapter(adapter);
        final EditText input = (EditText) getActivity().findViewById(R.id.editText);
        final Button button = (Button) getActivity().findViewById(R.id.button);
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
                            messages.add(new Message(android.R.drawable.ic_media_play,
                                    json.get("username") + "", json.get("text") + "", "00:00"));
                            adapter.notifyDataSetChanged();
                            getListView().setSelection(messages.size() - 1);
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
                        messages.add(new Message(android.R.drawable.ic_media_play,
                                username, text.trim(), "00:00"));
                        adapter.notifyDataSetChanged();
                        getListView().setSelection(messages.size() - 1);
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

    private class Message {

        int icon;
        String name;
        String message;
        String time;

        private Message(int icon, String name, String message, String time) {
            this.icon = icon;
            this.name = name;
            this.message = message;
            this.time = time;
        }
    }

    private class MessageAdapter extends ArrayAdapter {

        Context context;

        private MessageAdapter(Context context) {
            super(context, R.layout.chat_message_entry, messages);
            this.context = context;
        }

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_message_entry, null);
            }

            ImageView icon = (ImageView)convertView.findViewById(R.id.message_icon);
            TextView name = (TextView)convertView.findViewById(R.id.message_title);
            TextView message = (TextView)convertView.findViewById(R.id.message_content);
            TextView time = (TextView)convertView.findViewById(R.id.message_time);

            icon.setImageResource(messages.get(position).icon);
            name.setText(messages.get(position).name);
            message.setText(messages.get(position).message);
            time.setText(messages.get(position).time);

            return convertView;
        }
    }

}
