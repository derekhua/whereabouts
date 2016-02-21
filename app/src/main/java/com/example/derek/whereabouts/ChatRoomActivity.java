package com.example.derek.whereabouts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.facebook.appevents.AppEventsLogger;

public class ChatRoomActivity extends ActionBarActivity {

    int room;
    String roomName;
    String username;

    private String[] menuOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Intent intent = getIntent();
        room = intent.getIntExtra("ROOM_ID", -1);
        roomName = intent.getStringExtra("ROOM_NAME");
        username = intent.getStringExtra("USERNAME");
        setTitle("Room " + roomName);
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
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_toggle_night) {
            return true;
        } else if (id == R.id.action_map) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("ROOM_ID", room);
            intent.putExtra("ROOM_NAME", roomName);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
