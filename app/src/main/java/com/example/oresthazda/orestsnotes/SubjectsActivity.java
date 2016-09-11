package com.example.oresthazda.orestsnotes;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SubjectsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EDITOR_REQUEST_CODE = 1001;
    private CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);

        String[] from  = {DBIOpenHelper.SUBJECT_NAME};
        int[] to = {android.R.id.text1};

        cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0);


        ListView list = (ListView) findViewById(R.id.subjects_list);

        if(list != null) {

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(SubjectsActivity.this, MainActivity.class);

                    Uri uri = Uri.parse(SubjectsProvider.CONTENT_URI + "/" + id);
                    intent.putExtra(SubjectsProvider.CONTENT_ITEM_TYPE, uri);

                    startActivityForResult(intent, EDITOR_REQUEST_CODE);
                }
            });

            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {

                    TextView subjectOldName = (TextView) view.findViewById(android.R.id.text1);

                    editSubject(id, subjectOldName.getText().toString() );

                    return true;
                }
            });

            list.setAdapter(cursorAdapter);
        }



        getSupportLoaderManager().initLoader(0,null, this);
    }

    private void insertSubject(String subjectName) {
        ContentValues values = new ContentValues();
        values.put(DBIOpenHelper.SUBJECT_NAME, subjectName);

        Uri subjectUri = getContentResolver().insert(SubjectsProvider.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SubjectsProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        cursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }


    public void createSubject(View view) {

        //Alert actions handler
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {

                if (button == DialogInterface.BUTTON_POSITIVE) {

                    Dialog f = (Dialog) dialog;
                    EditText subjectName = (EditText) f.findViewById(R.id.subjectName);

                    if(subjectName.getText().length() > 0) {

                        insertSubject(subjectName.getText().toString());

                        restartLoader();

                        Toast.makeText(SubjectsActivity.this, "Create new subject: "+subjectName.getText().toString(), Toast.LENGTH_SHORT).show();

                    }

                }

            }

        };

        //Show alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        builder.setMessage(getString(R.string.are_you_sure))
                .setView(inflater.inflate(R.layout.new_subject, null))
                .setPositiveButton(getString(R.string.save), dialogClickListener)
                .setNegativeButton(getString(R.string.cancel), dialogClickListener)
                .show();


    }

    private void restartLoader() {

        getSupportLoaderManager().restartLoader(0, null, this);
    }


    private void editSubject(final long subjectId, final String subjectOldName) {

        //Alert actions handler
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {

                if (button == DialogInterface.BUTTON_POSITIVE) {

                    Dialog f = (Dialog) dialog;
                    EditText subjectName = (EditText) f.findViewById(R.id.subjectName);

                    if(subjectName.getText().length() > 0 && !subjectName.getText().toString().equals(subjectOldName) ) {

                        updateSubject(subjectId, subjectName.getText().toString());

                        restartLoader();

                    }

                }

            }

        };

        //Show alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View updateView = inflater.inflate(R.layout.modify_subject, null);

        EditText subjectNameTV = (EditText)updateView.findViewById(R.id.subjectName);
        subjectNameTV.setText(subjectOldName);

        builder.setMessage(getString(R.string.update_subject))
                .setView(updateView)
                .setPositiveButton(getString(R.string.save), dialogClickListener)
                .setNegativeButton(getString(R.string.cancel), dialogClickListener)
                .show();

    }

    private void updateSubject(long subjectId, String newName) {
        ContentValues values = new ContentValues();
        String selection = DBIOpenHelper.SUBJECT_ID + " = " + subjectId;

        values.put(DBIOpenHelper.SUBJECT_NAME, newName);

        getContentResolver().update(SubjectsProvider.CONTENT_URI, values, selection, null);

        Toast.makeText(this, "Subject updated", Toast.LENGTH_SHORT).show();

    }

}
