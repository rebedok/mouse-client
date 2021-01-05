package com.rebedok.remotecontrol;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rebedok.remotecontrol.pointer.PointerActivity;
import com.rebedok.remotecontrol.preference.MyPreferenceActivity;
import com.rebedok.remotecontrol.search.ServerSearch;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static int INTERNET_PERMISSION = 1001;
    private final static String INTERNET = Manifest.permission.INTERNET;
    private final static String MY_PREFERENCES = "MY PREFERENCE";
    private final static String IP_ADDRESS = "ipAddress";
    private final static int POINTER = 1;
    private final static int MOUSE = 2;
    private SharedPreferences preferences;
    private ServerSearch serverSearch;
    private EditText serverAddress;
    private ListView hostsAddresses;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> addresses;

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(IP_ADDRESS, serverAddress.getText().toString());
        editor.apply();
        serverSearch.stopSearch();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        preferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        serverAddress = (EditText) findViewById(R.id.ipAddress);
        serverAddress.setText(preferences.getString(IP_ADDRESS,""));
        initListView();
        if (!isPermissionGranted(INTERNET)) {
            requestPermission(INTERNET, INTERNET_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        serverSearch = new ServerSearch(adapter, addresses);
    }

    public void initListView() {
        hostsAddresses = (ListView) findViewById(R.id.addressesList);
        addresses = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addresses);
        hostsAddresses.setAdapter(adapter);
        hostsAddresses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                serverAddress.setText(((TextView) itemClicked).getText());
            }
        });
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    public void onPointer(View view) {
        if (serverAddress.length() != 0) {
            serverSearch.stopSearch();
            Intent intent = new Intent(MainActivity.this, PointerActivity.class);
            intent.putExtra(IP_ADDRESS, serverAddress.getText().toString());
            startActivityForResult(intent, POINTER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.main_settings:
                Intent intent = new Intent(MainActivity.this, MyPreferenceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == POINTER || requestCode == MOUSE) {
            if (resultCode == RESULT_OK) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Connection error", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


}
