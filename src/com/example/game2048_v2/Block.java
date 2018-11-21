package com.example.game2048_v2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

public class Block {

	// ������ �� GameView
	private GameView gameView;

	// ������� ������� � ������� ����
	private int x;
	private int y;

	// ������� ���� ��������� � ������� ����
	private int DestinationX;
	private int DestinationY;

	// ������� �� ������
	private int left;
	private int top;
	private int right;
	private int bottom;

	// ������� ���� ��������� �� ������
	private int DestinationLeft;
	private int DestinationTop;
	private int DestinationRight;
	private int DestinationBottom;

	// ������� ���������� ������ �����
	private int centerX;
	private int centerY;

	// ������� ���� ��� ���� ������� ������� ����� ��������
	private boolean DeleteAfterMove;
	volatile boolean Move;

	// ��������
	private int xSpeed;
	private int ySpeed;

	// ������ � ������
	private int EdgeOfRect;
	private int SizeOfText;

	// ����� �� �����
	private int Value;
	private int DestinationValue;

	// �����
	private Paint RectPaint;
	private Paint TextPaint;

	// ������ "�������������"
	private RectF rect;

	// �����������
	public Block(GameView gameView, int x, int y, int Value) {

		this.gameView = gameView;

		this.RectPaint = new Paint();
		this.RectPaint.setAntiAlias(true);
		this.RectPaint.setTextAlign(Paint.Align.CENTER);
		this.RectPaint.setStrokeWidth(5);
		this.RectPaint.setStyle(Paint.Style.FILL);

		this.TextPaint = new Paint();
		this.TextPaint.setAntiAlias(true);
		this.TextPaint.setColor(gameView.TextColor);
		this.TextPaint.setTextAlign(Paint.Align.CENTER);
		this.TextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

		set_X_Y(x, y);

		this.EdgeOfRect = gameView.EdgeOfRect; // ������ � ������

		setValue(Value);

		this.Move = false;
		this.rect = new RectF();
		CalcCanvasPosition();
		rect.set(left, top, right, bottom);
	}

	// ������������� ����������� ���������� ��� �������� ������
	void inithialize() {
		EdgeOfRect = gameView.EdgeOfRect;
		CalcCanvasPosition();
		rect.set(left, top, right, bottom);
		setSizeOfText();
	}

	// ������ ������� �� ������
	private void CalcCanvasPosition() {
		left = (x + 1) * gameView.SpaceBehoundRect + (x * EdgeOfRect);
		top = (y + 1) * gameView.SpaceBehoundRect + (y * EdgeOfRect);
		right = (x + 1) * gameView.SpaceBehoundRect + ((x + 1) * EdgeOfRect);
		bottom = (y + 1) * gameView.SpaceBehoundRect + ((y + 1) * EdgeOfRect);

		centerX = left + ((right - left) / 2);
		centerY = top + ((bottom - top) / 2);
	}

	// ��������
	private void update() {

		// ������ ������������� ��������
		if (DestinationX != x || DestinationY != y) {
			Move = true;

			if (DestinationX != x) {
				xSpeed = gameView.getSpeed() * ((DestinationX > x) ? 1 : -1);
			}
			;
			if (DestinationY != y) {
				ySpeed = gameView.getSpeed() * ((DestinationY > y) ? 1 : -1);
			}
			;
			DestinationLeft = (DestinationX + 1) * gameView.SpaceBehoundRect
					+ (DestinationX * EdgeOfRect);
			DestinationTop = (DestinationY + 1) * gameView.SpaceBehoundRect
					+ (DestinationY * EdgeOfRect);
			DestinationRight = (DestinationX + 1) * gameView.SpaceBehoundRect
					+ ((DestinationX + 1) * EdgeOfRect);
			DestinationBottom = (DestinationY + 1) * gameView.SpaceBehoundRect
					+ ((DestinationY + 1) * EdgeOfRect);
		}

		// ��������
		if (Move == true) {
			left += xSpeed;
			right += xSpeed;
			top += ySpeed;
			bottom += ySpeed;
			centerX += xSpeed;
			centerY += ySpeed;

			if ((xSpeed > 0
					&& (left > DestinationLeft || right > DestinationRight)
					|| (xSpeed < 0 && (left < DestinationLeft || right < DestinationRight))
					|| (ySpeed > 0 && (top > DestinationTop || bottom > DestinationBottom)) || (ySpeed < 0 && (top < DestinationTop || bottom < DestinationBottom)))) {
				left = DestinationLeft;
				right = DestinationRight;
				top = DestinationTop;
				bottom = DestinationBottom;
				centerX = left + ((right - left) / 2);
				centerY = top + ((bottom - top) / 2);
			}
			;
			rect.set(left, top, right, bottom);
		}

		// ��������� ��������
		if (Move == true) {
			if (left == DestinationLeft && top == DestinationTop
					&& right == DestinationRight && bottom == DestinationBottom) {
				Move = false;
				xSpeed = 0;
				ySpeed = 0;
				set_X_Y(DestinationX, DestinationY);
				setValue(getDestinationValue());
			}
		}
	}

	// ���������
	public void onDraw(Canvas canvas) {
		update(); // ������� ��� ��� ������� ��� ����� �������� ��� ������
					// ������
		canvas.drawRoundRect(rect, gameView.RoundOfRect, gameView.RoundOfRect,
				RectPaint);
		canvas.drawText(Integer.toString(Value), centerX,
				(int)(centerY + (SizeOfText / 2.8)), TextPaint);
	}

	// ���������� ������� ������� ��������
	public boolean isEmpty() {
		return (getValue() == 0);
	}

	// Getters {
	public int getValue() {
		return Value;
	}

	public int getDestinationValue() {
		return DestinationValue;
	}

	public int getDestinationX() {
		return DestinationX;
	}

	public int getDestinationY() {
		return DestinationY;
	}

	public boolean getDeleteAfterMove() {
		return DeleteAfterMove;
	}
	// } Getters

	// Setters {
	public void setDelete(boolean val) {
		DeleteAfterMove = val;
	}

	private void setColorByVal(int Val) {

		int Diff = 0;
		while (Val > 2) {
			Val /= 2;
			Diff++;
		}

		int G = Color.green(gameView.getColorOfBlock());
		int B = Color.blue(gameView.getColorOfBlock());
		int R = Color.red(gameView.getColorOfBlock());
		int ResG = G - (Diff * 20);
		int ResB = B;
		int ResR = R;
		if (ResG < B) {
			ResB = B + (B - ResG);
			ResG = B;
			if (ResB > 255) {
				ResR = R - (ResB - 255);
				ResB = 255;
			}
		}
		RectPaint.setColor(Color.rgb(ResR, ResG, ResB));

	}

	private void setSizeOfText() {
		if (Value <= 99) {
			SizeOfText = gameView.SizeOfText;
		}
		;
		if (Value > 99 && Value <= 999) {
			SizeOfText = gameView.SizeOfText * 2 / 3;
		}
		;
		if (Value > 999) {
			SizeOfText = gameView.SizeOfText * 2 / 5;
		}
		;
		TextPaint.setTextSize(SizeOfText);
	}

	public void setValue(int val) {
		if (Value != val) {
			Value = val;
			setDestinationValue(val);
			setColorByVal(val);
			setSizeOfText();
		}
	}

	public void setDestinationValue(int val) {
		DestinationValue = val;
	}

	private void set_X_Y(int vX, int vY) {
		x = vX;
		y = vY;
		DestinationX = vX;
		DestinationY = vY;
	}

	public void setDestination_X_Y(int vDestinationX, int vDestinationY) {
		DestinationX = vDestinationX;
		DestinationY = vDestinationY;
		if (vDestinationX != x || vDestinationY != y) {
			Move = true;
		}
	}
	// } Setters
}
