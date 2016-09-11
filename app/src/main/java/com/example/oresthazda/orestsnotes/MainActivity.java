package com.example.oresthazda.orestsnotes;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EDITOR_REQUEST_CODE = 1001;
    private CursorAdapter cursorAdapter;
    private String notesFilter;
    private String subjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(intent != null){
            Uri uri = intent.getParcelableExtra(SubjectsProvider.CONTENT_ITEM_TYPE);//get subject uri
            subjectId = uri.getLastPathSegment();
            notesFilter = DBIOpenHelper.NOTE_SUBJECT_ID + " = " + subjectId;
        }




        cursorAdapter = new NotesCursorAdapter(this, null, 0);

        ListView list = (ListView) findViewById(android.R.id.list);

        if(list != null) {
            list.setAdapter(cursorAdapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, NoteActivity.class);

                    Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                    intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);

                    startActivityForResult(intent, EDITOR_REQUEST_CODE);
                }
            });

        }



        getSupportLoaderManager().initLoader(0, null, this);

    }

    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBIOpenHelper.NOTE_TEXT, noteText);

        Uri noteUri = getContentResolver().insert(NotesProvider.CONTENT_URI, values);

    }

    /**
     * Manipulate with selection parameter noteFilter
     * so nice and easy
     * @param search Search query
     */
    public void getNotesListByKeyword(String search) {
        if(search.length() > 0) {
            notesFilter = notesFilter + " AND " + DBIOpenHelper.NOTE_TEXT + " LIKE '%"+search+"%'";
        }else {
            notesFilter = DBIOpenHelper.NOTE_SUBJECT_ID + " = " + subjectId;
        }
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextChange(String newText) {
                getNotesListByKeyword(newText);
                Log.d("MainActivity", "Text Changed");
                restartLoader();
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                getNotesListByKeyword(query);
                restartLoader();
                return false;
            }
        });

        return true;
    }

    // Handle menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_sample_data:
                insertSampleData();
                return true;
            case R.id.clear_data:
                clearSampleData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //Clear sample data
    private void clearSampleData() {

        //Alert actions handler
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {

                if (button == DialogInterface.BUTTON_POSITIVE) {

                    getContentResolver().delete(
                            NotesProvider.CONTENT_URI, null, null
                    );

                    restartLoader();

                    Toast.makeText(MainActivity.this, getString(R.string.clear_data), Toast.LENGTH_SHORT).show();

                }

            }

        };

        //Show alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener)
                .show();

    }


    //Insert sample data method
    private void insertSampleData() {
        insertNote("First note");
        insertNote("Some multi line \n super long note");
        insertNote("Very long note with a lot of content  that exceeds the with of the screen");
        restartLoader();
    }

    private void restartLoader() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesProvider.CONTENT_URI, null, notesFilter, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    public void createNewNote(View view) {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra("subject_id", subjectId );
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            getSupportLoaderManager().restartLoader(0, null, this);
        }
    }
}
