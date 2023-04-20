package com.filenko.conspectnote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.filenko.conspectnote.activity.EditNote;
import com.filenko.conspectnote.activity.EditQuestion;
import com.filenko.conspectnote.activity.cards.ActivityCards;
import com.filenko.conspectnote.activity.tests.ActivityTest;
import com.filenko.conspectnote.adapters.note.ItemTouchHelperCallback;
import com.filenko.conspectnote.adapters.note.NoteRecyclerViewAdapter;
import com.filenko.conspectnote.common.DownloadCard;
import com.filenko.conspectnote.common.FilesWorker;
import com.filenko.conspectnote.common.IntentStart;
import com.filenko.conspectnote.common.PermissionsHelper;
import com.filenko.conspectnote.common.ShowMessage;
import com.filenko.conspectnote.db.NoteDataBase;
import com.filenko.conspectnote.model.Note;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MainActivity extends AppCompatActivity {
    private NoteDataBase db;
    private NoteRecyclerViewAdapter adapter;
    private WebView tvNote;
    private final Note pasteNote = new Note();
    private LinearLayout linearLayoutMove;
    private String errorChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionsHelper.checkStoragePermission(this);
        errorChoice = String.valueOf(getResources().getString(R.string.error_choice_message));

        this.db = new NoteDataBase(this);

        setUI();
        initRecyclerView(db);
        setButton();
    }

    private void setUI() {
        this.tvNote = findViewById(R.id.tvNote);
        this.linearLayoutMove = findViewById(R.id.layoutMoveNote);
    }

    private void initRecyclerView(NoteDataBase dataBase) {
        RecyclerView listViewNote = findViewById(R.id.listViewNote);
        listViewNote.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this,
                R.drawable.divider1)));
        listViewNote.addItemDecoration(itemDecorator);

        this.adapter = new NoteRecyclerViewAdapter(dataBase, this);
        listViewNote.setAdapter(this.adapter);

        this.adapter.setOnClickListener((view, position) -> clickOnListView(position));

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this.adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(listViewNote);
    }

    private void clickOnListView(int position) {
        Note note = this.adapter.getItem(position);
        this.adapter.getNotesByIdParent(note.getId());
        this.adapter.setRootNote(note);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        clearView();
        try {
            setFields();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.top_menu_add:
                IntentStart.intentStartWithIntParam(this, EditNote.class,
                        "parent", this.adapter.getRootNote().getId());
                break;
            case R.id.top_menu_load_tree:
                onBrowse();
                break;
            case R.id.top_menu_export_tree:
                openDirectoryChooser();
                break;
            case R.id.top_menu_move:
                moveNote();
                break;
            case R.id.top_menu_delete:
                deleteNote();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void moveNote() {
        if (adapter.getRootNote().getId() > 0) {
            TextView tv = findViewById(R.id.titleNoteMove);
            this.pasteNote.clone(this.adapter.getRootNote());

            tv.setText(this.pasteNote.getTitle());
            linearLayoutMove.setVisibility(View.VISIBLE);
        } else {
            ShowMessage.showMessage(errorChoice, this);
        }
    }

    private void deleteNote() {
        if (adapter.getRootNote().getId() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                androidx.appcompat.app.AlertDialog dialog = noteDelete();
                dialog.show();
            }
        } else {
            ShowMessage.showMessage(errorChoice, this);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        clickOnButtonBack(this.adapter.getRootNote().getId());
    }

    @Override
    public boolean onSupportNavigateUp() {
        clickOnButtonBack(this.adapter.getRootNote().getParent());
        return true;
    }

    private void clickOnButtonBack(int idParent) {

        if (idParent > 0) {
            this.adapter.getNotesByIdParent(idParent);
            this.adapter.getNoteById(idParent);
            try {
                setFields();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.adapter.getRootNote().clear();
            this.adapter.getNotesByIdParent(0);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            clearView();
        }
    }

    private void setFields() throws UnsupportedEncodingException {
        this.tvNote.clearFormData();

        String stringBuilder = "<h2 align=\"center\"><font color=\"#008000\">" +
                adapter.getRootNote().getTitle() +
                "</font></h2>" +
                adapter.getRootNote().getHtml();
        this.tvNote.loadDataWithBaseURL(null, stringBuilder,
                "text/html", "UTF-8", null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveJsonTree(DocumentFile documentFile) {
        Note rootNote = adapter.getRootNote();


        List<Note> listNote = new ArrayList<>();
        if (rootNote.getParent() == 0) {
            listNote.add(rootNote);
        }
        this.db.loadTreeNotesByIdParent(listNote, rootNote.getId());
        this.db.getQuestionDataBase().loadQuestions(listNote);
        try {
            FilesWorker.exportJsonFile(
                    this,
                    this.adapter.getRootNote().getTitle(),
                    documentFile,
                    listNote);
        } catch (IOException e) {
            ShowMessage.showMessage(String.valueOf(R.string.error_choice_message), this);
        }

    }

    public void onBrowse() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        chooserIntent.addCategory(Intent.CATEGORY_OPENABLE);
        chooserIntent.setType("application/json");
        startActivityIntent.launch(chooserIntent);
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null) {
                        return;
                    }
                    try {
                        DownloadCard downloadCard = new DownloadCard(db, this, adapter);
                        downloadCard.extractJsonToBase(uri,
                                getResources().getString(R.string.app_name));
                    } catch (Exception e) {
                        ShowMessage.showMessage("Не удалось загрузить!", this);
                    }
                }
            });

    private void setButton() {
        findViewById(R.id.bottom_menu_edit).setOnClickListener(v -> {
            if (this.adapter.getRootNote().getId() > 0) {
                IntentStart.intentStartWithIntParam(
                        this, EditNote.class,
                        "key", this.adapter.getRootNote().getId());
            } else {
                ShowMessage.showMessage(errorChoice, this);
            }
        });

        findViewById(R.id.bottom_menu_question).setOnClickListener(v -> {
            if (this.adapter.getRootNote().getId() > 0) {
                IntentStart.intentStartWithIntParam(
                        this, EditQuestion.class,
                        "idnote", this.adapter.getRootNote().getId());
            } else {
                ShowMessage.showMessage(errorChoice, this);
            }
        });

        findViewById(R.id.bottom_menu_cards).setOnClickListener(v -> {
            if (this.adapter.getRootNote().getId() > 0) {
                IntentStart.intentStartWithIntParam(
                        this, ActivityCards.class,
                        "idnote", this.adapter.getRootNote().getId());
            } else {
                ShowMessage.showMessage(errorChoice, this);
            }
        });

        findViewById(R.id.bottom_menu_test).setOnClickListener(v -> {
            if (this.adapter.getRootNote().getId() > 0) {
                IntentStart.intentStartWithIntParam(
                        this, ActivityTest.class,
                        "idnote", this.adapter.getRootNote().getId());
            } else {
                ShowMessage.showMessage(errorChoice, this);
            }
        });

        findViewById(R.id.btnPasteNote).setOnClickListener(v -> {
            if (this.adapter.getRootNote().getId() != pasteNote.getId() &&
                    this.adapter.getRootNote().getId() != pasteNote.getParent()) {
                pasteNote.setParent(this.adapter.getRootNote().getId());
                pasteNote.setPosition(this.adapter.getItemCount());
                db.updateMoveNote(pasteNote, null);

                this.linearLayoutMove.setVisibility(View.GONE);
                this.adapter.addNote(pasteNote);
            }
        });

        findViewById(R.id.btnCancelPasteNote).setOnClickListener(v -> {
            this.linearLayoutMove.setVisibility(View.GONE);
            pasteNote.clear();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public androidx.appcompat.app.AlertDialog noteDelete() {
        return new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.title_when_delete_note))
                .setMessage(getResources().getString(R.string.message_when_delete_note))
                .setIcon(R.drawable.delete)
                .setPositiveButton(getResources().getString(R.string.yes), (dialog, whichButton) -> {
                    if (db.deleteNoteAndChild(adapter.getRootNote().getId())) {
                        clickOnButtonBack(adapter.getRootNote().getParent());
                    }
                    dialog.dismiss();
                }).setNegativeButton(
                        getResources().getString(R.string.cancel),
                        (dialog, which) -> dialog.dismiss())
                .create();
    }


    private void openDirectoryChooser() {
        if (this.adapter.getRootNote().getId() > 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityIntentChoice.launch(intent);
            }
        } else {
            ShowMessage.showMessage(errorChoice, this);
        }
    }

    ActivityResultLauncher<Intent> startActivityIntentChoice = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);
                        saveJsonTree(documentFile);
                    }
                }
            });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                           @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsHelper.checkResultPermissionsChoice(this, requestCode, permissions,
                grantResults);
    }

    private void clearView() {
        this.tvNote.loadUrl("about:blank");
    }
}