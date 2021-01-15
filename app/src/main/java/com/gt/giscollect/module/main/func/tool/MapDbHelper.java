package com.gt.giscollect.module.main.func.tool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zx.zxutils.util.ZXSystemUtil;

/**
 * Created by Xiangb on 2016/12/8.
 * 功能：
 */
public class MapDbHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    public static String TABLE_NAME;

    public MapDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        TABLE_NAME = ZXSystemUtil.getPackageName().substring(ZXSystemUtil.getPackageName().lastIndexOf(".") + 1);
    }

    public MapDbHelper(Context context, String name) {
        this(context, name, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(TILEINDEX TEXT," +
                "TILEDATA BLOB," +
                "TILETIME TEXT)");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists" + TABLE_NAME);
        onCreate(db);
    }

    public void delete() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.delete(MapDbHelper.TABLE_NAME, "1=1", null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
