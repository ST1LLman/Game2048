package com.example.game2048_v2;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class BestScoreActiviti extends Activity {

	DB db;
	SimpleCursorAdapter scAdapter;
	ListView lvData;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_best_score_activiti);

		db = new DB(this);
		db.open();
		
		// получаем курсор
		Cursor c = db.getAllData();
		startManagingCursor(c);

		// формируем столбцы сопоставления
		String[] from = new String[] { "Date", "Score" };
		int[] to = new int[] { R.id.tvDate, R.id.tvScore };

		// создаем адаптер и настраиваем список
		scAdapter = new SimpleCursorAdapter(this,
				R.layout.best_score_list_item, c, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		lvData = (ListView) findViewById(R.id.listView);
		lvData.setAdapter(scAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
	}

}
