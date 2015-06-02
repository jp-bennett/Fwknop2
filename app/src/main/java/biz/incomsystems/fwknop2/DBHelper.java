/*
This file is part of Fwknop2.

    Fwknop2 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package biz.incomsystems.fwknop2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 *
 * Created by jbennett on 5/29/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "fwknop.db";
    public static final String CONFIGS_TABLE_NAME = "configs";
    public static final String CONFIGS_COLUMN_ID = "id";
    public static final String CONFIGS_COLUMN_NICK_NAME = "NICK_NAME";
    public static final String CONFIGS_COLUMN_ACCESS_IP = "ACCESS_IP";
    public static final String CONFIGS_COLUMN_TCP_PORTS = "TCP_PORTS";
    public static final String CONFIGS_COLUMN_UDP_PORTS = "UDP_PORTS";
    public static final String CONFIGS_COLUMN_SERVER_IP = "SERVER_IP";
    public static final String CONFIGS_COLUMN_SERVER_PORT = "SERVER_PORT";
    public static final String CONFIGS_COLUMN_SERVER_TIMEOUT = "SERVER_TIMEOUT";
    public static final String CONFIGS_COLUMN_KEY = "KEY";
    public static final String CONFIGS_COLUMN_KEY_BASE64 = "KEY_BASE64";
    public static final String CONFIGS_COLUMN_HMAC = "HMAC";
    public static final String CONFIGS_COLUMN_HMAC_BASE64 = "HMAC_BASE64";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + CONFIGS_TABLE_NAME +
                        "(" +
                        CONFIGS_COLUMN_ID + " integer primary key, " +
                        CONFIGS_COLUMN_NICK_NAME + " text, " +
                        CONFIGS_COLUMN_ACCESS_IP + " text, " +
                        CONFIGS_COLUMN_TCP_PORTS + " text, " +
                        CONFIGS_COLUMN_UDP_PORTS + " text, " +
                        CONFIGS_COLUMN_SERVER_IP + " text, " +
                        CONFIGS_COLUMN_SERVER_PORT + " text, " +
                        CONFIGS_COLUMN_SERVER_TIMEOUT + " text, " +
                        CONFIGS_COLUMN_KEY + " text, " +
                        CONFIGS_COLUMN_KEY_BASE64 + " integer, " +
                        CONFIGS_COLUMN_HMAC + " text, " +
                        CONFIGS_COLUMN_HMAC_BASE64 + " integer" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This needs to be updated if we update the app in a way that changes the DB format
        db.execSQL("DROP TABLE IF EXISTS configs");
        onCreate(db);
    }

    public Cursor getData(String id) { // returns cursor so the config detail fragment can load a saved config
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from configs where NICK_NAME= '" + id + "'", null);
    }

    public boolean CheckNickIsUnique(String nick){
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "select * from configs where NICK_NAME= '" + nick + "'" ;
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean updateConfig  ( //  Automatically does an update or insert based on NICK_NAME
                                  String NICK_NAME, String ACCESS_IP, String TCP_PORTS,
                                  String UDP_PORTS, String SERVER_IP, String SERVER_PORT,
                                  String SERVER_TIMEOUT, String KEY, Boolean KEY_BASE64,
                                  String HMAC, Boolean HMAC_BASE64
                                  )
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NICK_NAME", NICK_NAME);
        contentValues.put("ACCESS_IP", ACCESS_IP);
        contentValues.put("TCP_PORTS", TCP_PORTS);
        contentValues.put("UDP_PORTS", UDP_PORTS);
        contentValues.put("SERVER_IP", SERVER_IP);
        contentValues.put("SERVER_PORT", SERVER_PORT);
        contentValues.put("SERVER_TIMEOUT", SERVER_TIMEOUT);
        contentValues.put("KEY", KEY);
        contentValues.put("KEY_BASE64", KEY_BASE64);
        contentValues.put("HMAC", HMAC);
        contentValues.put("HMAC_BASE64", HMAC_BASE64);

        if (CheckNickIsUnique(NICK_NAME)) {
            db.update("configs", contentValues, "NICK_NAME='" + NICK_NAME + "'", null);
        } else {
            db.insert("configs", null, contentValues);
        }

        return true;
    }

    public Integer deleteConfig (String nick) // Again, based on NICK_NAME
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("configs",
                "NICK_NAME =  '" + nick + "'", null );
    }

    public ArrayList<String> getAllConfigs()   // This returns an array of Nick Names in order
    {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from configs", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(CONFIGS_COLUMN_NICK_NAME)));
            res.moveToNext();
        }
        db.close();
        array_list.add("New Config");
        return array_list;
    }

}
