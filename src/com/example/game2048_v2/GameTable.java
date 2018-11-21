package com.example.game2048_v2;

import android.annotation.SuppressLint;
import android.graphics.Canvas;

@SuppressLint("WrongCall")
public class GameTable {

	// Ссылка на GameView
	private GameView gameView;
	
	// Объект для получения случайных значений
	private Randomizer Rnd;
	
	// Основная и временная матрицы с блоками
	private Block Blocks[][];
	private Block BlocksCopy[][];
	
	// Количество пустых ячеек
	private int QantOfEmptyVal;

	// Признак что происходит движение
	public volatile boolean isMove;

	// Конструктор
	public GameTable(GameView gameView, int RectInEdge) {
		this.gameView = gameView;
		
		Blocks = new Block[RectInEdge][RectInEdge];
		BlocksCopy = new Block[RectInEdge][RectInEdge];
		Rnd = new Randomizer();
	}

	// Инициализация графических параметров {
	void inithialize() {
		for (int i = 0; i < Blocks.length; i++) {
			for (int j = 0; j < Blocks[i].length; j++) {
				if (isEmptyPosition(i, j) != true) {
					inithializeBlock(i, j);
				}
			}
		}
	}

	public void inithializeBlock(int x, int y) {
		Blocks[x][y].inithialize();
	}
	// } Инициализация графических параметров 
	
	// Добавить случайный блок
	public boolean addRandomBlock(int Qant) {

		for (int q = 0; q < (Qant); q++) {
			QantOfEmptyVal = 0;
			for (int i = 0; i < Blocks.length; i++) {
				for (int j = 0; j < Blocks[i].length; j++) {
					if (isEmptyPosition(i, j)) {
						QantOfEmptyVal = QantOfEmptyVal + 1;
					}
				}
			}

			if (QantOfEmptyVal == 0) {
				return false;
			}
			;

			int PosOfNewVal = Rnd.GetRandomInt(QantOfEmptyVal - 1) + 1;

			QantOfEmptyVal = 0;
			for (int i = 0; i < Blocks.length; i++) {
				for (int j = 0; j < Blocks[i].length; j++) {
					if (isEmptyPosition(i, j)) {
						QantOfEmptyVal = QantOfEmptyVal + 1;
						if (QantOfEmptyVal == PosOfNewVal) {
							addBlock(i, j, Rnd.GetNewBlockValue());
							break;
						}
					}
				}
			}
		}

		return true;
	}

	// Рисование
	public void onDraw(Canvas canvas) {
		boolean visMove;
		visMove = false;
		for (int i = 0; i < Blocks.length; i++) {
			for (int j = 0; j < Blocks[i].length; j++) {
				if (isEmptyPosition(i, j) != true) {
					if (Blocks[i][j].Move == true) {
						visMove = true;
					}
					onDrawBlock(i, j, canvas);
				}
			}
		}
		if (isMove != visMove) {
			isMove = visMove;
		}
	}

	// Расчет движения блоков
	boolean moveTo(int MoveDir) {

		int startI = 0;
		int NoI = 0;
		int ModI = 0;
		int startJ = 0;
		int NoJ = 0;
		int ModJ = 0;
		boolean YAxis = false;

		if (MoveDir == GameView.LEFT) {
			startI = 0;
			NoI = gameView.RectInEdge;
			ModI = 1;
			startJ = 0;
			NoJ = gameView.RectInEdge;
			ModJ = 1;
			YAxis = true;
		}
		if (MoveDir == GameView.UP) {
			startI = 0;
			NoI = gameView.RectInEdge;
			ModI = 1;
			startJ = 0;
			NoJ = gameView.RectInEdge;
			ModJ = 1;
			YAxis = false;
		}
		if (MoveDir == GameView.RIGHT) {
			startI = gameView.RectInEdge - 1;
			NoI = -1;
			ModI = -1;
			startJ = 0;
			NoJ = gameView.RectInEdge;
			ModJ = 1;
			YAxis = true;
		}
		if (MoveDir == GameView.DOWN) {
			startI = 0;
			NoI = gameView.RectInEdge;
			ModI = 1;
			startJ = gameView.RectInEdge - 1;
			NoJ = -1;
			ModJ = -1;
			YAxis = false;
		}

		int exV;
		int V;
		int newV;
		int istep;
		int jstep;
		int exI;
		int exJ;

		exI = -1;
		exJ = -1;
				
		boolean returnVal = false;
		
		if (YAxis) {
			for (int j = startJ; (j != NoJ); j = j + ModJ) {

				jstep = j;
				exV = 0;
				istep = (ModI == 1) ? -1 : gameView.RectInEdge;
				for (int i = startI; (i != NoI); i = i + ModI) {

					if (isEmptyPosition(i, j) != true) {
						V = getBlockValue(i, j);
						// Если прошлое значение равно теперешнему то складываем
						// их
						if (V == exV) {
							newV = exV + V;
							exV = 0;
							setBlockDelete(exI, exJ, true);
						} else {
							istep += ModI;
							newV = V;
							exV = newV;
						}
						if (setBlockDestination_X_Y(i, j, istep, jstep)){
							returnVal = true;
						};
						setBlockDestinationValue(i, j, newV);
						exJ = j;
						exI = i;
					}
				}
			}
		} else {
			for (int i = startI; (i != NoI); i = i + ModI) {

				istep = i;
				exV = 0;
				jstep = (ModJ == 1) ? -1 : gameView.RectInEdge;
				for (int j = startJ; (j != NoJ); j = j + ModJ) {

					if (isEmptyPosition(i, j) != true) {
						V = getBlockValue(i, j);
						if (V == exV) {
							newV = exV + V;
							exV = 0;
							setBlockDelete(exI, exJ, true);
						} else {
							jstep += ModJ;
							newV = V;
							exV = newV;
						}
						;
						if (setBlockDestination_X_Y(i, j, istep, jstep)){
							returnVal = true;
						};
						setBlockDestinationValue(i, j, newV);
						exJ = j;
						exI = i;
					}
				}
			}
		}
		return returnVal;
	};

	// Обновление матрицы по результатам движения
	void ReloadBlockMatrix() {
		for (int i = 0; i < Blocks.length; i++) {
			for (int j = 0; j < Blocks[i].length; j++) {
				BlocksCopy[i][j] = null;
			}
		}

		for (int i = 0; i < Blocks.length; i++) {
			for (int j = 0; j < Blocks[i].length; j++) {
				if (isEmptyPosition(i, j) != true && getBlockDeleteAfterMove(i, j) != true){
						BlocksCopy[Blocks[i][j].getDestinationX()][Blocks[i][j].getDestinationY()] = Blocks[i][j];
				}
			}
		}

		for (int i = 0; i < Blocks.length; i++) {
			for (int j = 0; j < Blocks[i].length; j++) {
				Blocks[i][j] = BlocksCopy[i][j];
				BlocksCopy[i][j] = null;
			}
		}
	}

	// добавить блок
	private void addBlock(int x, int y, int Value) {
		Blocks[x][y] = new Block(gameView, x, y, Value);
	}

	// Рисование блока
	public void onDrawBlock(int x, int y, Canvas canvas) {
		Blocks[x][y].onDraw(canvas);
	}

	// Возвращает признак пустого блока
	public boolean isEmptyPosition(int x, int y) {
		return (Blocks[x][y] == null || Blocks[x][y].isEmpty());
	}
			
	// Setters {
	public void setBlockValue(int x, int y, int Value) {
		Blocks[x][y].setValue(Value);
	}

	public void setBlockDestinationValue(int x, int y, int Value) {
		if (Blocks[x][y].getDestinationValue() != Value) {
			Blocks[x][y].setDestinationValue(Value);
			gameView.addDestinationScore(Value);
		}
	}
	
	public void setSimpleStructure(int[][] Structure){
		for (int i = 0; i < Structure.length; i++) {
			for (int j = 0; j < Structure[i].length; j++) {
				if (Structure[i][j]!=0){
					addBlock(i, j, Structure[i][j]);
				}
			}
		}
	}
	
	public boolean setBlockDestination_X_Y(int x, int y, int DestinationX,
			int DestinationY) {
		if (x != DestinationX || y != DestinationY) {
			isMove = true;
			Blocks[x][y].setDestination_X_Y(DestinationX, DestinationY);
			return true;
		} else {
			return false;
		}
	}
	
	public void setBlockDelete(int x, int y, boolean Val) {
		Blocks[x][y].setDelete(Val);
	}
	// } Setters
	
	// Getters {
	public int getBlockValue(int x, int y) {
		return Blocks[x][y].getValue();
	}

	public int getBlockDestinationValue(int x, int y) {
		return Blocks[x][y].getDestinationValue();
	}
	
	private boolean getBlockDeleteAfterMove(int x, int y){
		return Blocks[x][y].getDeleteAfterMove();
	}
	
	public int[][] getSimpleStructure(){
		int[][] Structure = new int[gameView.RectInEdge][gameView.RectInEdge];
		
		for (int i = 0; i < Structure.length; i++) {
			for (int j = 0; j < Structure[i].length; j++) {
				Structure[i][j] = (Blocks[i][j]==null) ? 0 : getBlockValue(i, j);
			}
		}
		
		return Structure;
	}
	// } Getters
}
