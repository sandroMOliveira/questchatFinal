package com.android.rivchat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.android.rivchat.model.Friend;
import com.android.rivchat.model.ListFriend;
import com.android.rivchat.model.ListMatch;
import com.android.rivchat.model.Matchs;

public final class FriendDB {
    private static FriendDBHelper mDbHelper = null;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FriendDB() {
    }

    private static FriendDB instance = null;

    public static FriendDB getInstance(Context context) {
        if (instance == null) {
            instance = new FriendDB();
            mDbHelper = new FriendDBHelper(context);
        }
        return instance;
    }


    public long addFriend(Friend friend) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_ID, friend.id);
        values.put(FeedEntry.COLUMN_NAME_NAME, friend.name);
        values.put(FeedEntry.COLUMN_NAME_EMAIL, friend.email);
        values.put(FeedEntry.COLUMN_NAME_ID_ROOM, friend.idRoom);
        values.put(FeedEntry.COLUMN_NAME_AVATA, friend.avata);
        // Insert the new row, returning the primary key value of the new row
        return db.insert(FeedEntry.TABLE_NAME, null, values);
    }


    public long addMatch(Matchs matchs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(MatchEntry.COLUMN_NAME, matchs.getName());
        values.put(MatchEntry.COLUMN_NAME_ID, matchs.getIdFriend());
        values.put(MatchEntry.COLUMN_AVATA, matchs.getAvata());
        values.put(MatchEntry.COLUMN_NOTA, matchs.getNota());
        values.put(MatchEntry.COLUMN_APRENDER, matchs.getAprender());
        values.put(MatchEntry.COLUMN_ENSINAR, matchs.getEnsinar());
        values.put(MatchEntry.COLUMN_EMAIL_FRIEND, matchs.getEmailFriend());
        // Insert the new row, returning the primary key value of the new row
        return db.insert(MatchEntry.TABLE_NAME, null, values);
    }


    public void addListFriend(ListFriend listFriend){
        for(Friend friend: listFriend.getListFriend()){
            addFriend(friend);
        }
    }

    public void addListMatch(ListMatch listMatch){
        for(Matchs matchs: listMatch.getListMatchs()){
            addMatch(matchs);
        }
    }

    public ListFriend getListFriend() {
        ListFriend listFriend = new ListFriend();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        try {
            Cursor cursor = db.rawQuery("select * from " + FeedEntry.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                Friend friend = new Friend();
                friend.id = cursor.getString(0);
                friend.name = cursor.getString(1);
                friend.email = cursor.getString(2);
                friend.idRoom = cursor.getString(3);
                friend.materia = cursor.getString(4);
                friend.avata = cursor.getString(5);
                listFriend.getListFriend().add(friend);
            }
            cursor.close();
        }catch (Exception e){
            return new ListFriend();
        }
        return listFriend;
    }

    public ListMatch getListMatch() {
        ListMatch listMatch = new ListMatch();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        try {
            Cursor cursor = db.rawQuery("select * from " + MatchEntry.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                Matchs matchs = new Matchs();
                matchs.setIdFriend(cursor.getString(0));
                matchs.setName(cursor.getString(1));
                matchs.setNota(cursor.getLong(2));
                matchs.setEmailFriend(cursor.getString(3));
                matchs.setAvata(cursor.getString(4));
                matchs.setAprender(cursor.getString(5));
                matchs.setEnsinar(cursor.getString(6));
                listMatch.getListMatchs().add(matchs);
            }
            cursor.close();
        }catch (Exception e){
            return new ListMatch();
        }
        return listMatch;
    }

    public void deleteDB(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_MATCHS);
    }

    public void dropDB(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_MATCHS);
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES_MATCH);
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "friend";
        static final String COLUMN_NAME_ID = "friendID";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_EMAIL = "email";
        static final String COLUMN_NAME_ID_ROOM = "idRoom";
        static final String COLUMN_NAME_AVATA = "avata";
        static final String COLUMN_MATERIA = "materia";
    }

    public static class MatchEntry implements BaseColumns {
        static final String TABLE_NAME = "matchs";
        static final String COLUMN_NAME = "name_user";
        static final String COLUMN_NAME_ID = "friendID";
        static final String COLUMN_NOTA = "nota";
        static final String COLUMN_APRENDER = "aprender";
        static final String COLUMN_ENSINAR = "ensinar";
        static final String COLUMN_EMAIL_FRIEND = "email_friend";
        static final String COLUMN_AVATA = "avata";

    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_ID_ROOM + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_MATERIA + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_AVATA + TEXT_TYPE + " )";

    private static final String SQL_CREATE_ENTRIES_MATCH =
            "CREATE TABLE " + MatchEntry.TABLE_NAME + "(" +
                    MatchEntry.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    MatchEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_NOTA + "NUMBER" + COMMA_SEP +
                    MatchEntry.COLUMN_EMAIL_FRIEND + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_AVATA + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_APRENDER + TEXT_TYPE + COMMA_SEP +
                    MatchEntry.COLUMN_ENSINAR + TEXT_TYPE + " )";

    private static final String SQL_ERASE_TABLE =
            "DELETE * FROM " + MatchEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    private static final String SQL_DELETE_MATCHS =
            "DROP TABLE IF EXISTS " + MatchEntry.TABLE_NAME;


    private static class FriendDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "FriendChat.db";

        FriendDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
            db.execSQL(SQL_CREATE_ENTRIES_MATCH);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            db.execSQL(SQL_CREATE_ENTRIES_MATCH);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
