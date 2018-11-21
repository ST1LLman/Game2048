package com.example.game2048_v2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

public class MainActivity extends Activity {

	// Переменная Тэг для логов
	private static final String TAG = "game2048_v2";

	// Ссылка на GameView
	private GameView MainGameView;

	// Служебные переменные
	private GestureDetector gestureDetector;
	// private SharedPreferences sPref;
	private SharedPreferences Setting;
	DB db;
	SimpleDateFormat sdf;

	private String PrefKeyRectInEdge;
	private String PrefKeyRoundOfRect;
	private String PrefKeySpeed;

	// Жизненный цикл приложения...
	protected void onCreate(Bundle savedInstanceState) {
		Log("onCreate");
		super.onCreate(savedInstanceState);

		// без заголовка
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		PrefKeyRectInEdge = getString(R.string.pref_key_rect_in_edge);
		PrefKeyRoundOfRect = getString(R.string.pref_key_round_of_rect);
		PrefKeySpeed = getString(R.string.pref_key_speed);

		Setting = PreferenceManager.getDefaultSharedPreferences(this);

		MainGameView = new GameView(this, Integer.parseInt(Setting.getString(
				PrefKeyRectInEdge, getString(R.string.pref_default_rect_in_edge))));
		MainGameView.setLongClickable(true);

		setContentView(MainGameView);

		gestureDetector = initGestureDetector();
		MainGameView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		db = new DB(this);
		LoadData();
	}

	protected void onDestroy() {
		Log("onDestroy");
		super.onDestroy();
		SaveData();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log("onConfigurationChanged");
		MainGameView.reInithialize(newConfig);
	}

	
	protected void onPause() {
		Log("onPause");
		super.onPause();
		MainGameView.stopThread();
	}

	protected void onResume() {
		Log("onResume");
		super.onResume();

		int RectInEdge = Integer.parseInt(Setting.getString(PrefKeyRectInEdge,
				getString(R.string.pref_default_rect_in_edge)));
		if (RectInEdge != MainGameView.RectInEdge) {
			Log("RectInEdge = " + RectInEdge);
			MainGameView.RectInEdge = RectInEdge;
			MainGameView.GameStatus = GameView.GAME_IS_START;
		}
		
		double K_OfRoundOfRect = (double) 1
				/ Integer.parseInt(Setting.getString(PrefKeyRoundOfRect,
						getString(R.string.pref_default_round_of_rect)));
		if (K_OfRoundOfRect != MainGameView.K_OfRoundOfRect) {
			MainGameView.K_OfRoundOfRect = K_OfRoundOfRect;
		}
		
		int Speed = Integer.parseInt(Setting.getString(PrefKeySpeed,
						getString(R.string.pref_default_speed)));
		if (Speed != MainGameView.Speed) {
			MainGameView.Speed = Speed;
		}
		
		MainGameView.startThread();
	}

	protected void onSaveInstanceState(Bundle outState) {
		Log("onSaveInstanceState");
		super.onSaveInstanceState(outState);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(MainGameView.getSimpleStructure());
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			outState.putByteArray("SAVE_KEY_SimpleStructure", bos.toByteArray());
		}
		outState.putInt("SAVE_KEY_Score", MainGameView.getNowScore());
		outState.putInt("SAVE_KEY_BestScore", MainGameView.getBestScore());
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log("onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("SAVE_KEY_SimpleStructure")) {
				try {
					ObjectInputStream objectIn = new ObjectInputStream(
							new ByteArrayInputStream(savedInstanceState
									.getByteArray("SAVE_KEY_SimpleStructure")));
					Object obj = objectIn.readObject();
					MainGameView.setSimpleStructure((int[][]) obj);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			if (savedInstanceState.containsKey("SAVE_KEY_Score")) {
				MainGameView.setScore(savedInstanceState.getInt(
						"SAVE_KEY_Score", 0));
			}
			if (savedInstanceState.containsKey("SAVE_KEY_BestScore")) {
				MainGameView.setBestScore(savedInstanceState.getInt(
						"SAVE_KEY_BestScore", 0));
			}
			MainGameView.GameStatus = GameView.GAME_IS_CONTINUE;
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		Log((String) item.getTitle());

		int id = item.getItemId();
		if (id == R.id.action_BestScore) {
			startActivity(new Intent(this, BestScoreActiviti.class));
			return true;
		} else if (id == R.id.action_Setting) {
			startActivity(new Intent(this, SettingsActivity.class));
		} else if (id == R.id.action_Exit) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

	// Инициализация детектора жестов
	private GestureDetector initGestureDetector() {
		return new GestureDetector(this, new SimpleOnGestureListener() {

			private SwipeDetector detector = new SwipeDetector(
					MainGameView.SWIPE_MIN_DISTANCE,
					MainGameView.SWIPE_THRESHOLD_VELOCITY);

			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				int SwipeDirection = detector.getSwipeDirection(e1, e2,
						velocityX, velocityY);
				if (SwipeDirection > -1) {
					MainGameView.Move(SwipeDirection);
				}
				return false;
			}
			@Override
			public boolean onDown(MotionEvent e) {
				if (MainGameView.GameStatus == GameView.GAME_IS_LOOSE){
					SaveData();
					MainGameView.StartNewGame();
				}
				return false;
			}
		});
	}

	// Сохранение данных
	private void SaveData() {
		Log("SaveData");

		if (MainGameView.getScore() > 0) {
			db.open();
			long id = db.insert(getDateTextValue(), MainGameView.getScore());
			Log("Данные сохранены в БД - " + id);
			db.close();
		}
	}

	// Загрузка данных
	private void LoadData() {
		Log("LoadData");

		db.open();
		MainGameView.setBestScore(db.getBestScore());
		db.close();
	}

	// Логирование
	public static void Log(String v) {
		Log.d(TAG, v);
	}
	
	public static void Log(String v, String Tag) {
		Log.d(Tag, v);
	}

	@SuppressLint("SimpleDateFormat")
	private String getDateTextValue() {
		if (sdf == null)
			sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		String date = sdf.format(new Date(System.currentTimeMillis()));

		return date;
	}

	@Override
	public SharedPreferences getPreferences(int mode) {
		return super.getPreferences(mode);
	}

}