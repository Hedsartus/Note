package com.filenko.conspectnote.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.filenko.conspectnote.model.Answer;
import com.filenko.conspectnote.model.Question;

import java.util.ArrayList;
import java.util.List;

public class AnswerDataBase {
    private final DataBaseConnection connection;
    public static final String TABLE_ANSWER = "ANSWER";

    public AnswerDataBase(DataBaseConnection connection) {
        this.connection = connection;
    }

    public DataBaseConnection getConnection() {
        return this.connection;
    }

    public ContentValues getContentValues(Answer answer) {
        ContentValues dataValues = new ContentValues();
        dataValues.put("idquestion", answer.getIdQuestion());
        dataValues.put("title", answer.getAnswer());
        dataValues.put("correct", answer.isCorrect() ? 1 : 0);

        return dataValues;
    }

    public int saveAnswer (Answer answer, SQLiteDatabase database) {
        boolean isNeedClose = false;
        if(database == null) {
            database = this.connection.getWritableDatabase();
            isNeedClose = true;
        }

        int id;
        if (answer.getId() > 0) {
            id = database.update(TABLE_ANSWER, getContentValues(answer), "_id = ?",
                    new String[]{String.valueOf(answer.getId())});
        } else {
            id = (int) database.insert(TABLE_ANSWER, null, getContentValues(answer));
        }
        if(isNeedClose) {database.close();}

        return id;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Integer> getListIdAnswers(List<Integer> listQuestionId) {

        List<Integer> listAnswersId = new ArrayList<>();

        String sqlQuery = "SELECT * FROM ANSWER WHERE idquestion IN (" +
                DataBaseConnection.getStringFromArrayInt(listQuestionId) + ");";

        try (
                SQLiteDatabase database = this.connection.getWritableDatabase();
                Cursor query = database.rawQuery(sqlQuery, null)) {
            while (query.moveToNext()) {
                listAnswersId.add(query.getInt(0));
            }
        }
        return listAnswersId;
    }

    public void loadAnswersToQuestion(Question question, SQLiteDatabase database) {
        String sqlString = "SELECT * FROM ANSWER WHERE idquestion = " + question.getId() + ";";
        try (Cursor query = database.rawQuery(sqlString, null)) {
            while (query.moveToNext()) {
                question.addAnswer(new Answer(
                        query.getInt(0),
                        query.getInt(1),
                        query.getString(2),
                        query.getInt(3))
                );
            }

        }
    }

    public void setAnswersToQuestions(List<Question> questions) {
        SQLiteDatabase database = this.connection.getReadableDatabase();
        for (Question question : questions) {
            loadAnswersToQuestion(question, database);
        }
        database.close();
    }

}
