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
import java.util.UUID;


public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "fwknop.db";
    public static final int DATABASE_VERSION = 1;
    public static final String CONFIGS_TABLE_NAME = "configs";
    public static final String CONFIGS_COLUMN_ID = "id";
    public static final String CONFIGS_COLUMN_NICK_NAME = "NICK_NAME";
    public static final String CONFIGS_COLUMN_ACCESS_IP = "ACCESS_IP";
    public static final String CONFIGS_COLUMN_PORTS = "PORTS";
    public static final String CONFIGS_COLUMN_SERVER_IP = "SERVER_IP";
    public static final String CONFIGS_COLUMN_SERVER_PORT = "SERVER_PORT";
    public static final String CONFIGS_COLUMN_SERVER_TIMEOUT = "SERVER_TIMEOUT";
    public static final String CONFIGS_COLUMN_KEY = "KEY";
    public static final String CONFIGS_COLUMN_KEY_BASE64 = "KEY_BASE64";
    public static final String CONFIGS_COLUMN_HMAC = "HMAC";
    public static final String CONFIGS_COLUMN_NAT_IP = "NAT_IP";
    public static final String CONFIGS_COLUMN_NAT_PORT = "NAT_PORT";
    public static final String CONFIGS_COLUMN_SERVER_CMD = "SERVER_CMD";
    public static final String CONFIGS_COLUMN_HMAC_BASE64 = "HMAC_BASE64";
    public static final String CONFIGS_COLUMN_SSH_CMD = "SSH_CMD";
    public static final String CONFIGS_COLUMN_JUICE_UUID = "JUICE_UUID";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + CONFIGS_TABLE_NAME +
                        "(" +
                        CONFIGS_COLUMN_ID + " integer primary key, " +
                        CONFIGS_COLUMN_NICK_NAME + " text, " +
                        CONFIGS_COLUMN_ACCESS_IP + " text, " +
                        CONFIGS_COLUMN_PORTS + " text, " +
                        CONFIGS_COLUMN_SERVER_IP + " text, " +
                        CONFIGS_COLUMN_SERVER_PORT + " text, " +
                        CONFIGS_COLUMN_SERVER_TIMEOUT + " text, " +
                        CONFIGS_COLUMN_KEY + " text, " +
                        CONFIGS_COLUMN_KEY_BASE64 + " integer, " +
                        CONFIGS_COLUMN_HMAC + " text, " +
                        CONFIGS_COLUMN_NAT_IP + " text, " +
                        CONFIGS_COLUMN_NAT_PORT + " text, " +
                        CONFIGS_COLUMN_SERVER_CMD + " text, " +
                        CONFIGS_COLUMN_HMAC_BASE64 + " integer," +
                        CONFIGS_COLUMN_SSH_CMD + " text, " +
                        CONFIGS_COLUMN_JUICE_UUID + " text " +
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

    public boolean updateConfig  ( Context ctx, Config config)//  Automatically does either an update or insert based on NICK_NAME
    {
        // do more input validation here? return error?
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONFIGS_COLUMN_NICK_NAME, config.NICK_NAME);
        contentValues.put(CONFIGS_COLUMN_ACCESS_IP, config.ACCESS_IP);
        contentValues.put(CONFIGS_COLUMN_PORTS, config.PORTS);
        contentValues.put(CONFIGS_COLUMN_SERVER_IP, config.SERVER_IP);
        contentValues.put(CONFIGS_COLUMN_SERVER_PORT, config.SERVER_PORT);
        contentValues.put(CONFIGS_COLUMN_SERVER_TIMEOUT, config.SERVER_TIMEOUT);
        contentValues.put(CONFIGS_COLUMN_KEY, config.KEY);
        contentValues.put(CONFIGS_COLUMN_KEY_BASE64, config.KEY_BASE64);
        contentValues.put(CONFIGS_COLUMN_HMAC, config.HMAC);
        contentValues.put(CONFIGS_COLUMN_HMAC_BASE64, config.HMAC_BASE64);
        contentValues.put(CONFIGS_COLUMN_SERVER_CMD, config.SERVER_CMD);
        contentValues.put(CONFIGS_COLUMN_NAT_IP, config.NAT_IP);
        contentValues.put(CONFIGS_COLUMN_NAT_PORT, config.NAT_PORT);
        contentValues.put(CONFIGS_COLUMN_SSH_CMD, config.SSH_CMD);
        contentValues.put(CONFIGS_COLUMN_JUICE_UUID, config.juice_uuid.toString());

        if (CheckNickIsUnique(config.NICK_NAME)) {
            db.update("configs", contentValues, "NICK_NAME='" + config.NICK_NAME + "'", null);
        } else {
            db.insert("configs", null, contentValues);
        }
        db.close();
        return true;
    }

    public Integer deleteConfig (String nick) // Again, based on NICK_NAME
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("configs",
                "NICK_NAME =  '" + nick + "'", null );
    }

    public ArrayList<String> getAllConfigs()   // This returns an array of Nick Names
    {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from configs ORDER BY NICK_NAME COLLATE NOCASE", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(CONFIGS_COLUMN_NICK_NAME)));
            res.moveToNext();
        }
        res.close();
        db.close();
        //array_list.add("New Config");
        return array_list;
    }

    public Config getConfig (String nick) {
        Config config = new Config();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor CurrentIndex = db.rawQuery("select * from configs where NICK_NAME= '" + nick + "'", null);
        CurrentIndex.moveToFirst();
        config.ACCESS_IP = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_ACCESS_IP));
        config.PORTS = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_PORTS));
        config.SERVER_IP = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_IP));
        config.SERVER_PORT = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_PORT));
        config.SERVER_TIMEOUT = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_TIMEOUT));
        config.KEY = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_KEY));
        config.KEY_BASE64 = (CurrentIndex.getInt(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_KEY_BASE64)) == 1);
        config.HMAC = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_HMAC));
        config.HMAC_BASE64 = (CurrentIndex.getInt(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_HMAC_BASE64)) == 1);
        config.NAT_IP = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_NAT_IP));
        config.NAT_PORT = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_NAT_PORT));
        config.SERVER_CMD = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_CMD));
        config.SSH_CMD = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SSH_CMD));
        config.juice_uuid = UUID.fromString(CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_JUICE_UUID)));

        CurrentIndex.close();

        return config;
    }
}
