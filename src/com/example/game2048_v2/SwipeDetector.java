package com.example.game2048_v2;

import android.view.MotionEvent;

public class SwipeDetector {

	private int swipe_distance;
	private int swipe_velocity;

	public SwipeDetector(int distance, int velocity) {
		super();
		swipe_distance = distance;
		swipe_velocity = velocity;
	}

	public int getSwipeDirection(MotionEvent e1, MotionEvent e2,
			float velocityX, float velocityY) {

		int retVal = -1;

		float Xdistance = Math.abs(e1.getX() - e2.getX());
		float Ydistance = Math.abs(e1.getY() - e2.getY());

		if (Xdistance > Ydistance) {
			if (isSwipeLeft(e1, e2, velocityX)) {
				retVal = GameView.LEFT;
			}
			if (isSwipeRight(e1, e2, velocityX)) {
				retVal = GameView.RIGHT;
			}
		} else {
			if (isSwipeUp(e1, e2, velocityY)) {
				retVal = GameView.UP;
			}
			if (isSwipeDown(e1, e2, velocityY)) {
				retVal = GameView.DOWN;
			}
		}

		return retVal;
	}

	public boolean isSwipeDown(MotionEvent e1, MotionEvent e2, float velocityY) {
		return isSwipe(e2.getY(), e1.getY(), velocityY);
	}

	public boolean isSwipeUp(MotionEvent e1, MotionEvent e2, float velocityY) {
		return isSwipe(e1.getY(), e2.getY(), velocityY);
	}

	public boolean isSwipeLeft(MotionEvent e1, MotionEvent e2, float velocityX) {
		return isSwipe(e1.getX(), e2.getX(), velocityX);
	}

	public boolean isSwipeRight(MotionEvent e1, MotionEvent e2, float velocityX) {
		return isSwipe(e2.getX(), e1.getX(), velocityX);
	}

	private boolean isSwipeDistance(float coordinateA, float coordinateB) {
		return (coordinateA - coordinateB) > swipe_distance;
	}

	private boolean isSwipeSpeed(float velocity) {
		return Math.abs(velocity) > swipe_velocity;
	}

	private boolean isSwipe(float coordinateA, float coordinateB, float velocity) {
		return isSwipeDistance(coordinateA, coordinateB)
				&& isSwipeSpeed(velocity);
	}
}