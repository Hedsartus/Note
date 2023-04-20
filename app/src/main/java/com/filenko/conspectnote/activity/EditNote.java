package com.filenko.conspectnote.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.filenko.conspectnote.R;
import com.filenko.conspectnote.common.ChangeCheck;
import com.filenko.conspectnote.common.CircularLinkedList.CircularList;
import com.filenko.conspectnote.common.CircularLinkedList.CommandNote;
import com.filenko.conspectnote.common.ShowMessage;
import com.filenko.conspectnote.db.NoteDataBase;
import com.filenko.conspectnote.model.Note;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import jp.wasabeef.richeditor.RichEditor;


public class EditNote extends AppCompatActivity {
    private final Note note = new Note();
    private final ChangeCheck<Note> changeCheck = new ChangeCheck<>(note);
    private final NoteDataBase nDatabase = new NoteDataBase(this);
    private RichEditor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        createEditor();
        catchBundle();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menus, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.top_menu_save) {
            viewToEssence();
            saveOrUpdateNote();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveOrUpdateNote() {

        if(changeCheck.isChange(note)) {
            if(note.getTitle() != null && !note.getTitle().isBlank()) {
                String toastMsg =
                        note.getId() > 0 ? "Карточка успешно обновлена!" : "Карточка успешно добавлена!";
                int index = nDatabase.saveNote(this.note, null);
                if (note.getId() == 0) {
                    this.note.setId(index);
                }
                changeCheck.setChange(note);
                ShowMessage.showMessage(toastMsg, this);
            } else {
                ShowMessage.showMessage("Введите название записи!", this);
            }
        } else {
            ShowMessage.showMessage("Нет изменений!", this);
        }
    }

    private void viewToEssence() {
        this.note.setTitle(((EditText) findViewById(R.id.nodesName)).getText().toString());
        this.note.setHtml(mEditor.getHtml() == null ? "": mEditor.getHtml());
    }

    private void setFields() {
        ((EditText) findViewById(R.id.nodesName)).setText(this.note.getTitle());
        mEditor.setHtml(this.note.getHtml());
    }


    private void createEditor() {
        CircularList<CommandNote> colours = new CircularList<>();
        colours.add((e) -> e.setTextColor(Color.RED));
        colours.add((e) -> e.setTextColor(Color.GREEN));
        colours.add((e) -> e.setTextColor(Color.YELLOW));
        colours.add((e) -> e.setTextColor(Color.BLACK));

        CircularList<CommandNote> subSuperScriptCommands = new CircularList<>();
        subSuperScriptCommands.add(RichEditor::removeFormat);
        subSuperScriptCommands.add(RichEditor::setSubscript);
        subSuperScriptCommands.add(RichEditor::setSuperscript);

        mEditor = findViewById(R.id.editor);
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(18);
        mEditor.setEditorFontColor(Color.DKGRAY);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder("Введите текст записи...");

        findViewById(R.id.action_undo).setOnClickListener(v -> mEditor.undo());
        findViewById(R.id.action_redo).setOnClickListener(v -> mEditor.redo());

        findViewById(R.id.action_bold).setOnClickListener(v -> mEditor.setBold());
        findViewById(R.id.action_italic).setOnClickListener(v -> mEditor.setItalic());

        findViewById(R.id.action_subscript).setOnClickListener(v ->
            subSuperScriptCommands.getCurrent().command(mEditor));

        findViewById(R.id.action_strikethrough).setOnClickListener(v -> mEditor.setStrikeThrough());

        findViewById(R.id.action_underline).setOnClickListener(v -> mEditor.setUnderline());


        findViewById(R.id.action_align_left).setOnClickListener(v -> mEditor.setAlignLeft());
        findViewById(R.id.action_align_center).setOnClickListener(v -> mEditor.setAlignCenter());
        findViewById(R.id.action_align_right).setOnClickListener(v -> mEditor.setAlignRight());
        findViewById(R.id.action_insert_bullets).setOnClickListener(v -> mEditor.setBullets());

        findViewById(R.id.action_txt_color).setOnClickListener(v -> {
            colours.getCurrent().command(mEditor);
        });
    }

    private void catchBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getInt("key") > 0) {
            int idNote = bundle.getInt("key");
            if (idNote > 0) {
                this.note.setId(idNote);
                nDatabase.getNoteById(this.note.getId(), this.note);
                setFields();
                changeCheck.setChange(this.note);
            }
        } else if (bundle != null && bundle.getInt("parent") > 0) {
            this.note.setParent(bundle.getInt("parent"));
        }

    }

}
