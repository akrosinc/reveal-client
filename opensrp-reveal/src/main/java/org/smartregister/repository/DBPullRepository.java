package org.smartregister.repository;

import android.content.ContentValues;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class DBPullRepository extends BaseRepository {

    public static String DB_PULL_TABLE = "userconfig";

    public static String USERNAME = "username";

    private static final String CREATE_DB_PULL_TABLE =
            "CREATE TABLE " + DB_PULL_TABLE + " (" +
                    USERNAME + " VARCHAR NOT NULL" +
                    " ) ";


    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_DB_PULL_TABLE);
    }

    public void addUserToDbPullTable(String username) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERNAME, username);
        getWritableDatabase().insert(DB_PULL_TABLE, null, contentValues);
    }

    public void removeAllUsers() {
        getWritableDatabase().delete(DB_PULL_TABLE, "", null);
    }

    public List<String> getUsersInDBPull() {

        List<String> usernames = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * from " + DB_PULL_TABLE, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            String username = cursor.getString(cursor.getColumnIndex(USERNAME));

            usernames.add(username);

            cursor.moveToNext();
        }
        return usernames;

    }

}
