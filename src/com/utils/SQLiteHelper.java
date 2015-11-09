package com.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * @author pc
 *
 */
public class SQLiteHelper extends SQLiteOpenHelper{

	public static String TAG = "SQLiteHelper";
	
	public SQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate...");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade...");
//		String sql = "DROP TABLE IF EXISTS ";// + TABLE_NAME;
//		db.execSQL(sql);
//		onCreate(db);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	public long insert(String tableName, ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		return db.insert(tableName, null, values);
	}
	
	public void delete(String tableName, String where, String[] whereArgs){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(tableName, where, whereArgs);
	}
	
	public void update(String tableName, String where, String[] whereArgs, ContentValues values){
		SQLiteDatabase db = this.getWritableDatabase();
		db.update(tableName, values, where, whereArgs);
	}
	
	public Cursor query(String sql, String[] selectionArgs){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, selectionArgs);
		return cursor;
	}
}
