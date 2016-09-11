package com.example.oresthazda.orestsnotes;

import android.*;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private String action;

    private static final int REQUEST_LOCATION = 0;
    private static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String TAG = "NoteActivity";

    private EditText editor;
    private TextView date;
    private TextView locationTV;

    private String noteFilter;
    private String oldText;
    private String subjectId;
    private LocationManager locationManager;
    private String provider;

    private String latitude;
    private String longitude;

    private View myLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        myLayout = findViewById(R.id.my_layout);

        editor = (EditText) findViewById(R.id.editText);
        date = (TextView) findViewById(R.id.noteDate);
        locationTV = (TextView) findViewById(R.id.locationTv);

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        subjectId = intent.getStringExtra("subject_id");


        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle("New Note");
        } else {

            action = Intent.ACTION_EDIT;
            noteFilter = DBIOpenHelper.NOTE_ID + " = " + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri, DBIOpenHelper.ALL_NOTES_COLUMNS, noteFilter, null, null);

            cursor.moveToFirst();

            oldText = cursor.getString(cursor.getColumnIndex(DBIOpenHelper.NOTE_TEXT));
            String noteDate = cursor.getString(cursor.getColumnIndex(DBIOpenHelper.NOTE_CREATED));
            latitude = cursor.getString(cursor.getColumnIndex(DBIOpenHelper.NOTE_LATITUDE));
            longitude = cursor.getString(cursor.getColumnIndex(DBIOpenHelper.NOTE_LONGITUDE));

            editor.setText(oldText);
            editor.setSelection(oldText.length());
            date.setText(noteDate);

            Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
            try {
                List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
                if (!addresses.isEmpty()) {
                    locationTV.setText(addresses.get(0).getLocality());
                }

            } catch (IOException e) {
                Log.d(TAG, "Impossible to connect to Geocoder");
            }


            cursor.close();

        }

        //Get location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);

        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestLocationPermissions();

            } else {

                Location location = locationManager.getLastKnownLocation(provider);

                if (location != null) {
                    System.out.println("Provider " + provider + " has been selected.");
                    onLocationChanged(location);
                } else {
                    locationTV.setText(R.string.no_location);
                }

            }


        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermissions();

        } else {
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 400, 1, this);
            }
        }


    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermissions();

        } else {
            locationManager.removeUpdates(this);
        }
    }

    private void saveNote() {

        String newText = editor.getText().toString().trim();

        switch (action) {

            case Intent.ACTION_INSERT:
                if (newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newText);
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0) {

                    deleteNote();

                } else if (oldText.equals(newText)) {

                    setResult(RESULT_CANCELED);

                } else {
                    updateNote(newText);
                }


        }
        finish();

    }

    private void updateNote(String noteText) {

        ContentValues values = new ContentValues();
        values.put(DBIOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();


        values.put(DBIOpenHelper.NOTE_TEXT, noteText);
        values.put(DBIOpenHelper.NOTE_SUBJECT_ID, this.subjectId);



        values.put(DBIOpenHelper.NOTE_LATITUDE, latitude);
        values.put(DBIOpenHelper.NOTE_LONGITUDE, longitude);

        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);

    }

    private void deleteNote() {

        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);
        Toast.makeText(NoteActivity.this, "Note was deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                saveNote();
                break;
            case R.id.delete_note:
                deleteNote();
                return true;
            default:
                return true;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        saveNote();
        super.onBackPressed();
    }


    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (action.equals(Intent.ACTION_EDIT)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_note, menu);
        }

        return true;
    }


    //Location, Provider


    @Override
    public void onLocationChanged(Location location) {

       longitude = Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
        latitude = Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }


    //Request location permission
    private void requestLocationPermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {


            Log.i(TAG,
                    "Displaying contacts permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
            Snackbar.make(myLayout, R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(NoteActivity.this, PERMISSIONS_LOCATION,
                                            REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_LOCATION) {

            Log.i(TAG, "Received response for Location permission request.");

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "LOCATION permission has now been granted. Showing preview.");
                Snackbar.make(myLayout, R.string.permision_available_camera,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "LOCATION permission was NOT granted.");
                Snackbar.make(myLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }

        }else {

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
}
