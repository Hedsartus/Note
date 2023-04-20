package com.filenko.conspectnote.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.filenko.conspectnote.model.Note;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FilesWorker {

    public static String listToJson(List<?> list) {
        Type listType = new TypeToken<List<?>>() {}.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(list, listType);
    }

    public static void writeString(String jsonString, String namefile) {
        try (FileWriter writer = new FileWriter(namefile)) {
            writer.write(jsonString);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String readStringJson(String jsonText) {
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = (JSONArray) parser.parse(jsonText);
            return jsonObject.toJSONString();
        } catch (ParseException e) {
            Log.d("tag", e.getMessage());
        }
        return null;
    }

    public static <T> List<T> jsonToList(String jsonText, Class<T> elementType) throws Exception {
        List<T> list = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(jsonText);

        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            T object = gson.fromJson(String.valueOf(jsonObject), elementType);
            list.add(object);
        }
        return list;
    }

    public static boolean copyFiles(String source, String destination) {
        File inFile = new File(source);
        File destFile = new File(destination);
        if(!inFile.exists() && !destFile.exists() ) {
            return false;
        }

        try(FileInputStream fis = new FileInputStream(inFile)) {
            OutputStream output = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("Range")
    public static String getFileName(Uri contentUri, Context context) {
        String result = null;
        if (contentUri.getScheme() != null && contentUri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver()
                    .query(contentUri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = contentUri.getPath();
            if (result == null) {
                return null;
            }
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void exportJsonFile(Context context,
                                      String fileName,
                                      DocumentFile df,
                                      List<Note> listNote) throws IOException {

        DocumentFile file = df.findFile(fileName+".json");
        if(file == null) {
            file = df.createFile("application/json", fileName+".json");
        }

        OutputStream outputStream = context.getContentResolver().openOutputStream(file.getUri());
        outputStream.write(listToJson(listNote).getBytes());
        outputStream.flush();
        outputStream.close();

    }
}
