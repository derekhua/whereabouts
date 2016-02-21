package com.example.derek.whereabouts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    public final static String PREFS = "sharedPrefsString";
    public final static String SAVEDID = "savedID";
    public final static String DISPLAYNAME = "displayName";
    public final static String NUMROOMS = "numberOfRooms";

    String localUser;
    List<Chat> chats = new ArrayList<>();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent in = new Intent(this, GpsService.class);
        startService(in);

        sharedPref = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        String restoredText = sharedPref.getString(SAVEDID, null);
        localUser = sharedPref.getString(DISPLAYNAME, "user");

        FacebookSdk.sdkInitialize(this.getApplicationContext());

        // Facebook tracker
        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                this.stopTracking();
                Profile.setCurrentProfile(currentProfile);
                Log.d("facebook ---", Profile.getCurrentProfile().getName());
                localUser = Profile.getCurrentProfile().getName();
                Toast.makeText(MainActivity.this, localUser, Toast.LENGTH_SHORT).show();
                sharedPref = getSharedPreferences(DISPLAYNAME, Context.MODE_PRIVATE);

                editor.putString(DISPLAYNAME, localUser);
                editor.apply();
            }
        };
        profileTracker.startTracking();

        FacebookSdk.sdkInitialize(this.getApplicationContext());

        Log.d("facebook ---", "main activity");

        if (restoredText == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        // Initialize chat room list
        final ListView listView = (ListView) findViewById(R.id.listView);
        final ArrayAdapter adapter = new ChatAdapter(this);
        listView.setAdapter(adapter);
        for (int i = 0; i < sharedPref.getInt(NUMROOMS, 0); i++) {
            // TODO Server request
            String roomId = sharedPref.getString("" + i, null);
            String roomName = sharedPref.getString(roomId, null);
            chats.add(new Chat(android.R.drawable.ic_media_play, roomId, roomName));
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chat chat = (Chat) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Joining chatroom " + chat.name,
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(), ChatRoomActivity.class);
                intent.putExtra("ROOM_ID", chat.id);
                intent.putExtra("ROOM_NAME", chat.name);
                intent.putExtra("USERNAME", localUser);
                startActivity(intent);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_new_room) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Create new room");
            builder.setMessage("Enter room name (4 to 20 characters):");
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int button) {
                    String newRoom = input.getText().toString().trim();

                    // ID generator
                    StringBuilder tmp = new StringBuilder();
                    for (char ch = '0'; ch <= '9'; ++ch)
                        tmp.append(ch);
                    for (char ch = 'A'; ch <= 'Z'; ++ch)
                        tmp.append(ch);
                    char[] symbols = tmp.toString().toCharArray();
                    String id = "";
                    Random random = new Random();
                    for (int i = 0; i < 6; i++) {
                        id += (symbols[random.nextInt(symbols.length)]);
                    }

                    Toast.makeText(getApplicationContext(), "Room \"" + newRoom +
                            "\" has been created", Toast.LENGTH_SHORT).show();
                    chats.add(new Chat(android.R.drawable.ic_media_play,
                            id, newRoom));

                    int numRooms = sharedPref.getInt(NUMROOMS, 0);

                    editor.putString(id, newRoom);
                    editor.putString("" + numRooms, id);
                    editor.putInt(NUMROOMS, numRooms + 1);
                    editor.apply();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            // Enable "Done" button only if room name is valid
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String str = s.toString();
                    if (str.length() < 4 || str.length() > 20) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            });
            return true;
        } else if (id == R.id.action_join_room) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Join existing room");
            builder.setMessage("Enter room ID (10 characters):");
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int button) {
                    String id = input.getText().toString().trim();

                    // TODO Server request

                    String newRoom = "click to get name";

                    Toast.makeText(getApplicationContext(), "Room \"" + newRoom +
                            "\" has been joined", Toast.LENGTH_SHORT).show();
                    chats.add(new Chat(android.R.drawable.ic_media_play,
                            id.toUpperCase(), newRoom));


                    int numRooms = sharedPref.getInt(NUMROOMS, 0);

                    editor.putString(id, newRoom);
                    editor.putString("" + numRooms, id);
                    editor.putInt(NUMROOMS, numRooms + 1);
                    editor.apply();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            // Enable "Done" button only if room name is valid
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String str = s.toString();
                    if (str.length() < 4 || str.length() > 20) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }

                }
            });
            return true;
        } else if (id == R.id.action_toggle_night) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.themeName, outValue, true);
            if ("Light".equals(outValue.string)) {
                setTheme(R.style.DarkTheme);
            } else {
                setTheme(R.style.LightTheme);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.derek.whereabouts/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.derek.whereabouts/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class Chat {

        int icon;
        String id;
        String name;

        private Chat(int icon, String id, String name) {
            this.icon = icon;
            this.id = id;
            this.name = name;
        }
    }

    private class ChatAdapter extends ArrayAdapter {

        Context context;

        private ChatAdapter(Context context) {
            super(context, R.layout.chat_room_entry, chats);
            this.context = context;
        }

        @Override
        public int getCount() {
            return chats.size();
        }

        @Override
        public Object getItem(int position) {
            return chats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_room_entry, null);
            }

            ImageView icon = (ImageView) convertView.findViewById(R.id.chat_icon);
            TextView name = (TextView) convertView.findViewById(R.id.chat_title);

            icon.setImageResource(chats.get(position).icon);
            name.setText(chats.get(position).name);

            return convertView;
        }
    }
}
