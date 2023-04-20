package com.filenko.conspectnote.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.stream.Collectors;


public class DataBaseConnection extends SQLiteOpenHelper {
    private static final String DB_NAME = "nodebase.db"; // Имя базы данных
    private static final int DB_VERSION = 1; // Версия базы данных

    public DataBaseConnection(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE NOTES ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "position INTEGER, "
                + "parent INTEGER, "
                + "title TEXT NOT NULL, "
                + "html TEXT);");

        db.execSQL("CREATE TABLE QUESTIONS ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "type INTEGER  NOT NULL, "
                + "idnote INTEGER  NOT NULL, "
                + "title TEXT NOT NULL, "
                + "correct INTEGER);");

        db.execSQL("CREATE TABLE ANSWER ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "idquestion INTEGER  NOT NULL, "
                + "title TEXT NOT NULL,"
                + "correct INTEGER DEFAULT 0 );");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int insert(ContentValues values, String table, SQLiteDatabase database) {
        boolean isNeedClose = false;
        if (database == null) {
            database = this.getWritableDatabase();
            isNeedClose = true;
        }
        int id = (int) database.insert(table, null, values);


        if (isNeedClose) database.close();
        return id;
    }

    public boolean deleteFromId(int id, String table, SQLiteDatabase database) {
        boolean isNeedClose = false;
        if (database == null) {
            database = getWritableDatabase();
            isNeedClose = true;
        }

        int delCount = database.delete(table, "_id =" + id, null);
        if (isNeedClose) {
            database.close();
        }
        return delCount > 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getStringFromArrayInt(List<Integer> list) {
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
