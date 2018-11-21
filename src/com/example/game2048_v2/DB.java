package com.example.game2048_v2;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DB {

	final static String DB_NAME = "MyDB"; // имя БД
	final static int DB_VERSION = 1; // версия БД

	final String HIGHT_SCORE_TABLE_NAME = "HightScore";
	final String ID_FIELD_NAME = "_id";
	final String DATE_FIELD_NAME = "Date";
	final String SCORE_FIELD_NAME = "Score";

	private Context context;

	private DBHelper mDBHelper;
	private SQLiteDatabase mDB;

	public DB(Context ctx) {
		context = ctx;
	}

	// открыть подключение
	public void open() {
		mDBHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
		mDB = mDBHelper.getWritableDatabase();
	}

	// закрыть подключение
	public void close() {
		if (mDBHelper != null)
			mDBHelper.close();
	}

	public Cursor getAllData() {
		return mDB.query(HIGHT_SCORE_TABLE_NAME, null, null, null, null, null,
				SCORE_FIELD_NAME + " DESC");
	}
	
	public long insert (String Date, int Score){
		ContentValues cv = new ContentValues();
		cv.put(DATE_FIELD_NAME, Date);
		cv.put(SCORE_FIELD_NAME, Score);
		long id =  mDB.insert(HIGHT_SCORE_TABLE_NAME, null, cv);
		return id;
	}
	
	public int getBestScore(){
		Cursor c = mDB.query(HIGHT_SCORE_TABLE_NAME,
				new String[] { "MAX("+SCORE_FIELD_NAME+") as "+SCORE_FIELD_NAME }, null, null, null, null,
				null);

		if (c != null) {
			if (c.moveToFirst()) {
				return c.getInt(c.getColumnIndex(SCORE_FIELD_NAME));
			}
		}
		return 0;
	}

	// класс по созданию и управлению БД
	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		public void onCreate(SQLiteDatabase db) {
			MainActivity.Log("--- onCreate database ---");

			if (context == null) {
				return;
			}

			// создаем таблицу с полями
			db.execSQL("create table " + HIGHT_SCORE_TABLE_NAME + " ("
					+ ID_FIELD_NAME +" integer primary key autoincrement," + DATE_FIELD_NAME+ " text, "
					+ SCORE_FIELD_NAME + " integer" + ");");

			// Первоначальное заполнение БД из данных старого формата
			SharedPreferences sPref;
			sPref = ((MainActivity) context)
					.getPreferences(MainActivity.MODE_PRIVATE);
			int vBestScore = sPref.getInt("BestScore", 0);
			if (vBestScore > 0) {
				ContentValues cv = new ContentValues();
				cv.put(DATE_FIELD_NAME, "");
				cv.put(SCORE_FIELD_NAME, vBestScore);
				db.insert(SCORE_FIELD_NAME, null, cv);

				// Очищаем старые данные
				Editor ed = sPref.edit();
				ed.clear();
				ed.commit();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}
}
