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

import com.facebook.ProfileTracker;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;


public class MainActivity extends AppCompatActivity {

    public final static String USERNAME = "username";
    String localUser;
    List<Chat> chats = new ArrayList<>();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    Profile facebookProfile;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        sharedPref = getSharedPreferences(USERNAME, Context.MODE_PRIVATE);
        String restoredText = sharedPref.getString(USERNAME, null);
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        // Facebook tracker
        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                this.stopTracking();
                Profile.setCurrentProfile(currentProfile);
                Log.d("facebook ---", Profile.getCurrentProfile().getName());
                localUser = Profile.getCurrentProfile().getName();
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
        String[] values = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        for (int i = 0; i < values.length; ++i) {
            chats.add(new Chat(android.R.drawable.ic_media_play, values[i]));
        }
        final ArrayAdapter adapter = new ChatAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((Chat) listView.getItemAtPosition(position)).name;
                Toast.makeText(getApplicationContext(), "Joining chatroom " + item,
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(), ChatRoomActivity.class);
                intent.putExtra("ROOM_ID", Integer.parseInt(item));
                intent.putExtra("ROOM_NAME", item);
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
        String name;

        private Chat(int icon, String name) {
            this.icon = icon;
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
