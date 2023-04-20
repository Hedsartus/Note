package com.filenko.conspectnote.common;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.filenko.conspectnote.adapters.note.NoteRecyclerViewAdapter;
import com.filenko.conspectnote.db.AnswerDataBase;
import com.filenko.conspectnote.db.NoteDataBase;
import com.filenko.conspectnote.db.QuestionDataBase;
import com.filenko.conspectnote.model.Answer;
import com.filenko.conspectnote.model.Note;
import com.filenko.conspectnote.model.Question;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DownloadCard {
    private final NoteDataBase db;
    private final Context context;
    private final NoteRecyclerViewAdapter adapter;
    private ProgressDialog progressDialog;

    public DownloadCard(NoteDataBase db, Context ctx, NoteRecyclerViewAdapter adapter) {
        this.db = db;
        this.context = ctx;
        this.adapter = adapter;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void extractJsonToBase(Uri uri, String appName) throws Exception {
        String fileName = FilesWorker.getFileName(uri, this.context);

        String filePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS) + File.separator +
                appName + File.separator + fileName;

        String jsonJava = FilesWorker.readStringJson(readTextFromUri(uri));

        List<Note> listNote = FilesWorker.jsonToList(jsonJava, Note.class);

        if (listNote.size() > 0) {
            DownloadNote eerf = new DownloadNote();
            eerf.execute((ArrayList<Note>) listNote);
        }
    }

    private void createNote(Note note, int parent, Map<Integer, Integer> idOldNewNote, SQLiteDatabase database) {
        note.setParent(parent);

        // вносим в мапу ключ старый id и новый id
        int newNoteId = this.db.insert(this.db.getContentValues(note), NoteDataBase.TABLE_NOTE, database);
        idOldNewNote.put(note.getId(), newNoteId);

        createQuestion(note, newNoteId, database);
    }

    private void createQuestion(Note note, int newId, SQLiteDatabase database) {
        // проходимся по списку вопросов узла
        for (Question question : note.getListQuestion()) {
            // меняем старый idNote у вопросов на новый idNote
            question.setIdNote(newId);

            // меняем у вопросов id после внесения в бд
            question.setId(this.db.insert(db.getQuestionDataBase().getContentValues(question),
                    QuestionDataBase.TABLE_QUESTIONS, database));

            // проходимся по всем ответам вопроса
            for (Answer answer : question.getListAnswers()) {
                // фиксируем у ответов новый idQuestion
                answer.setIdQuestion(question.getId());

                // добавляем в бд ответ
                answer.setId(
                        this.db.insert(
                                db.getQuestionDataBase()
                                        .getAnswerDataBase().getContentValues(answer),
                                AnswerDataBase.TABLE_ANSWER, database));
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class DownloadNote extends AsyncTask<ArrayList<Note>, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Создаем и отображаем диалоговое окно прогресса
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Загрузка карточек...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(ArrayList<Note>... args) {
            ArrayList<Note> noteList = args[0];

            int totalCards = noteList.size();
            int currentProgress = 0;

            SQLiteDatabase database = db.getReadableDatabase();

            Map<Integer, Integer> idOldNewNote = new HashMap<>();

            createNote(noteList.get(0), adapter.getRootNote().getId(), idOldNewNote, database);
            List<Note> tempNote = new ArrayList<>();

            for (int i = 1; i < noteList.size(); i++) {
                Note note = noteList.get(i);
                // if there is a parent in the map:
                if (idOldNewNote.containsKey(note.getParent())) {
                    createNote(note, idOldNewNote.get(note.getParent()), idOldNewNote, database);
                } else {
                    if (note.getParent() > 0) {
                        tempNote.add(note);
                    } else {
                        // if there is no parent
                        createNote(note, adapter.getRootNote().getId(), idOldNewNote, database);
                    }
                }

                currentProgress++;
                publishProgress((int) ((currentProgress / (float) totalCards) * 100));
            }

            for (Note n : tempNote) {
                if (idOldNewNote.containsKey(n.getParent())) {
                    createNote(n, idOldNewNote.get(n.getParent()), idOldNewNote, database);
                } else {
                    createNote(n, adapter.getRootNote().getId(), idOldNewNote, database);
                }
            }
            database.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            adapter.getNotesByIdParent(adapter.getRootNote().getId());
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
}
