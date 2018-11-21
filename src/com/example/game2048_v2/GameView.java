package com.example.game2048_v2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint({ "WrongCall", "ViewConstructor" })
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	// Статические переменные {
	public static final int LEFT = 0;
	public static final int UP = 1;
	public static final int RIGHT = 2;
	public static final int DOWN = 3;

	public static final int GAME_IS_START = 0;
	public static final int GAME_IS_CONTINUE = 1;
	public static final int GAME_IS_LOOSE = 2;
	// } Статические переменные

	// Графические переменные {
	private int canvasWidth;
	private int canvasHeight;
	public int EdgeOfRect;
	public int SizeOfText;
	int RoundOfRect;
	int SpaceBehoundRect;
	int RectInEdge;
	double K_OfRoundOfRect;
	// } Графические переменные

	// Игровое поле
	private volatile GameTable gameTable;

	// Поток в котором происходит рисование
	private GameThread mThread;
	private SurfaceHolder sfh;

	// Переменная запускающая поток рисования
	private volatile boolean running = false;

	// Количество очков
	private int BestScore;
	private int Score;
	private int DestinationScore;
	private int ScoreDiff;

	public int GameStatus;

	private boolean isInithialize;

	// Признак необходимости добавить блок
	private boolean addBlock;

	// Служебные переменные {
	private RectF rect = new RectF();
	private RectF RectScore = new RectF();
	private RectF RectBestScore = new RectF();
	private RectF RectLoose = new RectF();
	private Paint p = new Paint();
	private Paint TextPaint = new Paint();
	private Paint TextPaintLoose = new Paint();
	// } Служебные переменные

	// Настройки {
	int BackgroundColor = getResources().getColor(R.color.background);
	int RectColor = getResources().getColor(R.color.rect);
	int ColorOfBlock = getResources().getColor(R.color.block);
	int TextColor = getResources().getColor(R.color.text);
	int Speed;
	final int QantOfBlockOnStart = 2;
	final int QantOfBlockOnTurn = 1;
	final int SWIPE_MIN_DISTANCE = 60;
	final int SWIPE_THRESHOLD_VELOCITY = 20;
	final double K_OfSpaceBehoundRect = 0.05;

	// } Настройки

	// Конструктор
	public GameView(Context context, int RectInEdge) {
		super(context);

		MainActivity.Log("new GameView");

		this.RectInEdge = RectInEdge;
		
		isInithialize = false;

		sfh = this.getHolder();
		sfh.addCallback(this);

		p.setAntiAlias(true);
		p.setColor(RectColor);
		p.setStrokeWidth(5);
		p.setStyle(Paint.Style.FILL);

		this.TextPaint = new Paint();
		this.TextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
		this.TextPaint.setAntiAlias(true);
		this.TextPaint.setColor(TextColor);
		this.TextPaint.setTextSize(SizeOfText);
		this.TextPaint.setTextAlign(Paint.Align.CENTER);
		
		this.TextPaintLoose = new Paint();
		this.TextPaintLoose.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
		this.TextPaintLoose.setAntiAlias(true);
		this.TextPaintLoose.setColor(TextColor);
		this.TextPaintLoose.setTextSize(SizeOfText);
		this.TextPaintLoose.setTextAlign(Paint.Align.CENTER);

		gameTable = new GameTable(this, RectInEdge);

		/* Рисуем все наши объекты и все все все */
		getHolder().addCallback(new SurfaceHolder.Callback() {
			/*** Уничтожение области рисования */
			public void surfaceDestroyed(SurfaceHolder holder) {
				MainActivity.Log("surfaceDestroyed");
			}

			/** Создание области рисования */
			public void surfaceCreated(SurfaceHolder holder) {
				MainActivity.Log("surfaceCreated");
			}

			/** Изменение области рисования */
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
		});
	}

	// Класс Поток в котором происходит рисование
	@SuppressLint("WrongCall")
	public class GameThread extends Thread {

		/** Конструктор класса */
		public GameThread() {
			MainActivity.Log("new GameThread");
		}

		/** Задание состояния потока */
		public void setRunning(boolean run) {
			running = run;
		}

		/** Действия, выполняемые в потоке */
		public void run() {
			while (running) {
				if (isInithialize) {
					Canvas canvas = null;
					try {
						// подготовка Canvas-а
						canvas = sfh.lockCanvas();
						// собственно рисование
						if (canvas != null) {
							onDraw(canvas);
						}
						// }
					} catch (Exception e) {
					} finally {
						if (canvas != null) {
							sfh.unlockCanvasAndPost(canvas);
						}
					}
				}
			}
		}
	}

	// Работа с потоком {
	public void startThread() {
		MainActivity.Log("startThread");

		mThread = new GameThread();
		mThread.setRunning(true);
		mThread.start();
	}

	public void stopThread() {
		MainActivity.Log("stopThread");

		boolean retry = true;
		mThread.setRunning(false);
		while (retry) {
			try {
				// ожидание завершение потока
				mThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	// } Работа с потоком

	// Переопределенные процедуры {
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isInithialize = false;
	}

	public void surfaceCreated(SurfaceHolder holder) {

		if (GameStatus == GameView.GAME_IS_START) {
			StartNewGame();
			isInithialize = false;
		}

		Canvas canvas = null;
		while (!isInithialize) {
			try {
				canvas = sfh.lockCanvas();
				inithialize(canvas, null);
			} catch (Exception e) {
			} finally {
				if (canvas != null) {
					sfh.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	// } Переопределенные процедуры

	
	public void reInithialize(Configuration newConfig){
		isInithialize = false;
		Canvas canvas = null;
		try {
			canvas = sfh.lockCanvas();
			inithialize(canvas, newConfig);
		} catch (Exception e) {
		} finally {
			if (canvas != null) {
				sfh.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	public void StartNewGame(){
		MainActivity.Log("GAME_IS_START");
		gameTable = new GameTable(this, RectInEdge);
		setScore(0);
		gameTable.addRandomBlock(QantOfBlockOnStart);
		GameStatus = GameView.GAME_IS_CONTINUE;
	}
	
	// Рисование
	protected void onDraw(Canvas canvas) {

		if (addBlock == true && gameTable.isMove == false) {
			gameTable.ReloadBlockMatrix();
			gameTable.addRandomBlock(QantOfBlockOnTurn);
			addBlock = false;
			if (isLoose()){
				GameStatus = GAME_IS_LOOSE;
				MainActivity.Log("GAME_IS_LOOSE", "d");
			}
		}

		canvas.drawColor(BackgroundColor);

		for (int i = 0; i < RectInEdge; i++) {
			for (int j = 0; j < RectInEdge; j++) {
				rect.set((i + 1) * SpaceBehoundRect + i * EdgeOfRect, j
						* (EdgeOfRect) + SpaceBehoundRect * (j + 1), (i + 1)
						* SpaceBehoundRect + (i + 1) * EdgeOfRect, (j + 1)
						* (EdgeOfRect) + SpaceBehoundRect * (j + 1));
				canvas.drawRoundRect(rect, RoundOfRect, RoundOfRect, p);
			}
		}

		// Подсчет очков
		ScoreDiff = DestinationScore - Score;
		if (ScoreDiff > 2000) {
			Score += 100;
		} else if (ScoreDiff > 1000) {
			Score += 50;
		} else if (ScoreDiff > 100) {
			Score += 5;
		} else if (ScoreDiff > 50) {
			Score += 2;
		} else if (ScoreDiff > 0) {
			Score += 1;
		}

		if (Score > BestScore) {
			BestScore = Score;
		}

		// Поле очков
		canvas.drawRoundRect(RectScore, RoundOfRect, RoundOfRect, p);
		canvas.drawText(Integer.toString(Score), RectScore.left
				+ ((RectScore.right - RectScore.left) / 2), (int) (RectScore.top
				+ ((RectScore.bottom - RectScore.top) / 2) + (SizeOfText / 2.8)),
				TextPaint);

		// Поле лучших очков
		canvas.drawRoundRect(RectBestScore, RoundOfRect, RoundOfRect, p);
		canvas.drawText(Integer.toString(BestScore), RectBestScore.left
				+ ((RectBestScore.right - RectBestScore.left) / 2),
				(int) (RectBestScore.top
						+ ((RectBestScore.bottom - RectBestScore.top) / 2)
						+ (SizeOfText / 2.8)), TextPaint);

		gameTable.onDraw(canvas);
		
		if (GameStatus == GAME_IS_LOOSE){
			canvas.drawRoundRect(RectLoose, RoundOfRect, RoundOfRect, p);
			canvas.drawText(getResources().getString(R.string.loose_text), RectLoose.left
					+ ((RectLoose.right - RectLoose.left) / 2), (int) (RectLoose.top
					+ ((RectLoose.bottom - RectLoose.top) / 2) + (TextPaintLoose.getTextSize() / 2.8)),
					TextPaintLoose);
			}
	}

	// Движение
	public void Move(int MoveDir) {
		MainActivity.Log("Move - " + MoveDir);

		gameTable.isMove = true;
		if (gameTable.moveTo(MoveDir)) {
			addBlock = true;
		}
	}

	private int convertDpToPixel(int dp){
	    DisplayMetrics metrics = getResources().getDisplayMetrics();
	    int px = (int) (dp * (metrics.densityDpi / 160f));
	    return px;
	}
	
	// Инициализация графических параметров при создании холста
	private void inithialize(Canvas canvas, Configuration newConfig) {
		MainActivity.Log("inithialize GameView");

		if (newConfig != null){
			canvasWidth = convertDpToPixel(newConfig.screenWidthDp);
			canvasHeight = convertDpToPixel(newConfig.screenHeightDp);
		}else{
			canvasWidth = canvas.getWidth(); // Ширина
			canvasHeight = canvas.getHeight(); // Высота
		}
		
		int minEdge;

		if (canvasWidth > canvasHeight) {
			minEdge = canvasHeight;
		} else {
			minEdge = canvasWidth;
		}

		EdgeOfRect = (int) Math
				.round((minEdge / (double) (RectInEdge + (double) (RectInEdge + 1)
						* K_OfSpaceBehoundRect)));

		RoundOfRect = (int) (EdgeOfRect * K_OfRoundOfRect);
		SpaceBehoundRect = (int) (EdgeOfRect * K_OfSpaceBehoundRect);

		setSizeOfText(EdgeOfRect * 2 / 3);

		String LooseText = getResources().getString(R.string.loose_text);
		TextPaintLoose.setTextSize((EdgeOfRect*RectInEdge) / (LooseText.length()+2));
		
		if (canvasHeight > canvasWidth) {
			int RectScoreHeight = (canvasHeight
					- ((SpaceBehoundRect + EdgeOfRect) * RectInEdge) - (SpaceBehoundRect * 6)) / 2;
			int RectScoreWidth = (EdgeOfRect * RectInEdge)
					+ (SpaceBehoundRect * (RectInEdge - 1));

			// Поле очков
			RectScore.set(SpaceBehoundRect,
					((SpaceBehoundRect + EdgeOfRect) * RectInEdge)
							+ (SpaceBehoundRect * 2), SpaceBehoundRect
							+ RectScoreWidth,
					((SpaceBehoundRect + EdgeOfRect) * RectInEdge)
							+ (SpaceBehoundRect * 2) + RectScoreHeight);

			// Поле лучших очков
			RectBestScore.set(SpaceBehoundRect,
					((SpaceBehoundRect + EdgeOfRect) * RectInEdge)
							+ (SpaceBehoundRect * 4) + RectScoreHeight,
					SpaceBehoundRect + RectScoreWidth,
					((SpaceBehoundRect + EdgeOfRect) * RectInEdge)
							+ (SpaceBehoundRect * 4) + (RectScoreHeight * 2));
			
			// Поле проигрыша
			RectLoose.set(canvasWidth/4, canvasWidth/4 , canvasWidth*3/4, canvasWidth*3/4);
		} else {
			// Поле очков
			RectScore.set((RectInEdge * (EdgeOfRect + SpaceBehoundRect))
					+ (SpaceBehoundRect * 2), SpaceBehoundRect, canvasWidth
					- (SpaceBehoundRect * 2), SpaceBehoundRect + EdgeOfRect);

			// Поле лучших очков
			RectBestScore.set((RectInEdge * (EdgeOfRect + SpaceBehoundRect))
					+ (SpaceBehoundRect * 2),
					SpaceBehoundRect * 2 + EdgeOfRect, canvasWidth
							- (SpaceBehoundRect * 2),
					(SpaceBehoundRect + EdgeOfRect) * 2);
			
			// Поле проигрыша
			RectLoose.set(canvasHeight/4, canvasHeight/4 , canvasHeight*3/4, canvasHeight*3/4);
		}

		gameTable.inithialize();

		isInithialize = true;
	}

	public void addDestinationScore(int Val) {
		DestinationScore = DestinationScore + Val;
	}

	boolean isLoose(){
		
		boolean val = true;
		
		for (int i = 0; i < RectInEdge; i++) {
			for (int j = 0; j < RectInEdge; j++) {
				if (gameTable.isEmptyPosition(i, j)){
					val = false;
					break;
				}
			} 
			if (!val) break;
		} 
		
		if (val){
			
			int ax;
			int bx;
			int ay;
			int by;
			for (int i = 0; i < RectInEdge; i++) {
				ax = 0;
				bx = 0;
				ay = 0;
				by = 0;
				for (int j = 0; j < RectInEdge; j++) {
					bx = gameTable.getBlockValue(i, j);
					if (ax == bx){val = false; break;}
					ax = bx;
					
					by = gameTable.getBlockValue(j, i);
					if (ay == by){val = false; break;};
					ay = by;
				} 
				if (!val) break;
			}
		}
		
		return val;
	}
	
	// Getters {
	public int getSpeed() {
		return Speed;
	}
	
	public void setSizeOfText(int Val) {
		SizeOfText = Val;
		TextPaint.setTextSize(SizeOfText);
	}

	public int getBestScore() {
		return BestScore;
	}

	public void setBestScore(int Val) {
		BestScore = Val;
	}

	public int getScore() {
		return Score;
	}

	public int getNowScore() {
		if (DestinationScore > Score) {
			return DestinationScore;
		} else {
			return Score;
		}
	}

	public int getColorOfBlock() {
		return ColorOfBlock;
	}

	public int[][] getSimpleStructure() {
		return gameTable.getSimpleStructure();
	}

	// } Getters

	// Setters {
	public void setRunning(boolean run) {
		mThread.setRunning(run);
	}

	public void setScore(int Val) {
		Score = Val;
		DestinationScore = Val;
	}

	public void setSimpleStructure(int[][] Val) {
		gameTable.setSimpleStructure(Val);
	}
	// } Setters
}
