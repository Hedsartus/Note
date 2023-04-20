package com.filenko.conspectnote.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.filenko.conspectnote.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteDataBase extends DataBaseConnection {
    public final static String TABLE_NOTE = "NOTES";
    private final QuestionDataBase questionDataBase;

    public NoteDataBase(Context context) {
        super(context);
        this.questionDataBase = new QuestionDataBase(this);
    }

    public QuestionDataBase getQuestionDataBase() {
        return this.questionDataBase;
    }

    public int saveNote(Note note, SQLiteDatabase database) {
        boolean isNeedClose = false;
        if (database == null) {
            database = this.getWritableDatabase();
            isNeedClose = true;
        }

        int id;
        if (note.getId() > 0) {
            id = database.update(TABLE_NOTE, getContentValues(note), "_id = ?",
                    new String[]{String.valueOf(note.getId())});
        } else {
            id = (int) database.insert(
                    TABLE_NOTE, null, getContentValues(note));
        }
        if (isNeedClose) {
            database.close();
        }

        return id;
    }

    public void updateMoveNote(Note note, SQLiteDatabase database) {
        boolean isNeedClose = false;
        if (database == null) {
            database = this.getWritableDatabase();
            isNeedClose = true;
        }

        ContentValues dataValues = new ContentValues();
        dataValues.put("position", note.getPosition());
        dataValues.put("parent", note.getParent());

        database.update(TABLE_NOTE, dataValues, "_id = ?",
                new String[]{String.valueOf(note.getId())});

        if (isNeedClose) {
            database.close();
        }
    }

    public ContentValues getContentValues(Note note) {
        ContentValues dataValues = new ContentValues();
        dataValues.put("position", note.getPosition());
        dataValues.put("parent", note.getParent());
        dataValues.put("title", note.getTitle());
        dataValues.put("html", note.getHtml());

        return dataValues;
    }

    public List<Note> getRootNotes() {
        List<Note> objects = new ArrayList<>();

        try (
                SQLiteDatabase database = this.getReadableDatabase();
                Cursor query = database.rawQuery(
                        "SELECT * FROM NOTES WHERE parent = 0 ORDER BY position;", null)
        ) {
            while (query.moveToNext()) {
                objects.add(new Note(
                        query.getInt(0),
                        query.getInt(1),
                        query.getInt(2),
                        query.getString(3),
                        query.getString(4)));
            }
        }
        return objects;
    }

    public void getNotesByIdParent(int idParent, List<Note> objects) {
        if (idParent >= 0) {
            objects.clear();
            String sql = "SELECT * FROM NOTES WHERE parent = " + idParent + " ORDER BY position;";

            try (
                    SQLiteDatabase database = this.getReadableDatabase();
                    Cursor query = database.rawQuery(sql, null)
            ) {
                while (query.moveToNext()) {
                    objects.add(new Note(
                            query.getInt(0),
                            query.getInt(1),
                            query.getInt(2),
                            query.getString(3),
                            query.getString(4)));
                }
            }
        }
    }

    /**
     * Get Note from database by id
     * @param id (id row Note)
     * @param note (note where add params)
     *
     */
    public void getNoteById(int id, Note note) {
        if (id > 0) {
            String sqlString = "SELECT * FROM NOTES WHERE _id = " + id + ";";
            try (
                    SQLiteDatabase database = this.getReadableDatabase();
                    Cursor query = database.rawQuery(sqlString, null)
            ) {
                while (query.moveToNext()) {
                    note.setId(id);
                    note.setParent(query.getInt(2));
                    note.setPosition(query.getInt(1));
                    note.setTitle(query.getString(3));
                    note.setHtml(query.getString(4));
                }
            }
        }
    }

    /**
     * Get notes from parent
     * @param idRoot (id parent)
     * add Notes to List<Note> listNote
    * **/
    public void loadTreeNotesByIdParent(List<Note> listNote, int idRoot) {
        try (SQLiteDatabase database = this.getReadableDatabase()) {
            String sql = "WITH recursive " +
                    "  Parrent_Id(n) AS ( " +
                    "    VALUES(" + idRoot + ") " +
                    "    UNION " +
                    "    SELECT _id FROM NOTES, Parrent_Id WHERE NOTES.parent = Parrent_Id.n) " +
                    "SELECT _id, position, parent, title, html FROM NOTES " +
                    "WHERE NOTES._id IN Parrent_Id AND NOTES.parent != 0 ORDER BY _id";

            try (Cursor q = database.rawQuery(sql, null)) {
                while (q.moveToNext()) {
                    listNote.add(new Note(
                            q.getInt(0),
                            q.getInt(1),
                            q.getInt(2),
                            q.getString(3),
                            q.getString(4)));
                }
            }
        } catch (SQLiteException ex) {
            Log.d("___ERROR: ", ex.getMessage());
        }

    }

    /**
     * Get list ID children tree Note (recursive sql query)
     *
     * @param id (Note)
     * @return List<Integer> idNotes
     */
    public List<Integer> getListIdChildrenByParentNote(int id) {
        List<Integer> listNote = new ArrayList<>();

        String sql = "WITH recursive " +
                "  Parrent_Id(n) AS ( " +
                "    VALUES(" + id + ") " +
                "    UNION " +
                "    SELECT _id FROM NOTES, Parrent_Id WHERE NOTES.parent = Parrent_Id.n) " +
                "SELECT _id FROM NOTES " +
                "WHERE NOTES._id IN Parrent_Id AND NOTES.parent != 0 ORDER BY _id";

        try (SQLiteDatabase database = this.getReadableDatabase();
             Cursor q = database.rawQuery(sql, null)) {
            while (q.moveToNext()) {
                listNote.add(q.getInt(0));
            }
        } catch (SQLiteException ex) {
            Log.d("___ERROR: ", ex.getMessage());
        }

        return listNote;
    }

    public void updateNotePosition(Note note, int changePosition, SQLiteDatabase database) {
        boolean isNeedClose = false;
        if (database == null) {
            database = this.getWritableDatabase();
            isNeedClose = true;
        }

        ContentValues dataValues = new ContentValues();
        dataValues.put("position", changePosition);

        database.update(TABLE_NOTE, dataValues, "_id = ?",
                new String[]{String.valueOf(note.getId())});
        if (isNeedClose) database.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean deleteNoteAndChild(int idNote) {
        boolean isDeleted;
        List<Integer> listNote = getListIdChildrenByParentNote(idNote);
        listNote.add(idNote);
        List<Integer> questionIdList = questionDataBase.loadTreeIntegerQuestionFromDatabaseByIdNotes(listNote);
        List<Integer> answersIdList = questionDataBase.getAnswerDataBase().getListIdAnswers(questionIdList);

        SQLiteDatabase database = this.getWritableDatabase();

        database.beginTransaction();
        try {
            for (Integer idAnswer : answersIdList) {
                deleteFromId(idAnswer, AnswerDataBase.TABLE_ANSWER, database);
            }

            for (Integer idQuestion : questionIdList) {
                deleteFromId(idQuestion, QuestionDataBase.TABLE_QUESTIONS, database);
            }

            for (Integer id : listNote) {
                deleteFromId(id, TABLE_NOTE, database);
            }

            database.setTransactionSuccessful();
            isDeleted = true;
        } catch (SQLiteException e) {
            isDeleted = false;
        } finally {
            database.endTransaction();
        }

        return isDeleted;
    }
}
