package com.filenko.conspectnote.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.filenko.conspectnote.model.Note;
import com.filenko.conspectnote.model.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionDataBase {
    private final DataBaseConnection connection;
    private final AnswerDataBase answerDataBase;
    public static final String TABLE_QUESTIONS = "QUESTIONS";

    public QuestionDataBase(DataBaseConnection connection) {
        this.connection = connection;
        this.answerDataBase = new AnswerDataBase(this.connection);
    }

    public DataBaseConnection getConnection() {
        return this.connection;
    }

    public AnswerDataBase getAnswerDataBase() {
        return this.answerDataBase;
    }

    public ContentValues getContentValues(Question question) {
        ContentValues dataValues = new ContentValues();
        dataValues.put("idnote", question.getIdNote());
        dataValues.put("type", question.getType());
        dataValues.put("title", question.getTitle());
        dataValues.put("correct", question.getCorrect() ? 1 : 0);

        return dataValues;
    }

    public int saveQuestion (Question question, SQLiteDatabase database) {
        boolean isNeedClose = false;
        if(database == null) {
            database = this.connection.getWritableDatabase();
            isNeedClose = true;
        }

        int id;
        if (question.getId() > 0) {
            id = database.update(TABLE_QUESTIONS, getContentValues(question), "_id = ?",
                    new String[]{String.valueOf(question.getId())});
        } else {
            id = (int) database.insert(TABLE_QUESTIONS, null, getContentValues(question));
        }
        if(isNeedClose) {database.close();}

        return id;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Integer> loadTreeIntegerQuestionFromDatabaseByIdNotes(List<Integer> listNotes) {
        String sql = "SELECT _id, idnote FROM QUESTIONS WHERE idnote IN ("
                + DataBaseConnection.getStringFromArrayInt(listNotes) + ");";
        List<Integer> listQuestion = new ArrayList<>();

        try (
                SQLiteDatabase database = this.connection.getReadableDatabase();
                Cursor query = database.rawQuery(sql, null)
        ) {
            while (query.moveToNext()) {
                listQuestion.add(query.getInt(0));
            }
        }
        return listQuestion;
    }

    public void loadQuestions(List<Note> listNote) {
        SQLiteDatabase database = this.connection.getReadableDatabase();
        for (Note note : listNote) {
            String sql = "SELECT * FROM QUESTIONS WHERE idnote = " + note.getId();
            try (Cursor q = database.rawQuery(sql, null)) {
                while (q.moveToNext()) {
                    Question question = new Question(
                            q.getInt(0),
                            q.getInt(1),
                            q.getInt(2),
                            q.getString(3),
                            q.getInt(4));
                    this.answerDataBase.loadAnswersToQuestion(question, database);
                    note.addChild(question);
                }
            }
        }
        database.close();
    }

    public void loadTreeQuestions(int idNote, List<Question> questions) {
        String sql = "SELECT * FROM QUESTIONS WHERE idnote IN ( " +
                "WITH recursive " +
                "  Parrent_Id(n) AS ( " +
                "    VALUES(" + idNote + ") " +
                "    UNION " +
                "    SELECT _id FROM NOTES, Parrent_Id WHERE NOTES.parent = Parrent_Id.n) " +
                "SELECT _id FROM NOTES " +
                "WHERE NOTES._id IN Parrent_Id)";

        try (
                SQLiteDatabase database = this.connection.getReadableDatabase();
                Cursor q = database.rawQuery(sql, null)
        ) {
            while (q.moveToNext()) {
                questions.add(new Question(
                        q.getInt(0),
                        q.getInt(1),
                        q.getInt(2),
                        q.getString(3),
                        q.getInt(4)));
            }
        }
    }

    public boolean deleteQuestion(int id, SQLiteDatabase database) {
        boolean flagCloseDb = false;
        if(database == null) {
            database = this.connection.getWritableDatabase();
            flagCloseDb = true;
        }

        List<Integer> listAnswersId = new ArrayList<>();

        try (Cursor query = database.rawQuery(
                "SELECT * FROM ANSWER WHERE idquestion = " + id + ";", null)) {
            while (query.moveToNext()) {
                listAnswersId.add(query.getInt(0));
            }
        }

        for (Integer val : listAnswersId) {
            connection.deleteFromId(val, AnswerDataBase.TABLE_ANSWER, database);
        }

        boolean result = connection.deleteFromId(id, TABLE_QUESTIONS, database);

        if(flagCloseDb) {database.close();}

        return result;
    }

}
