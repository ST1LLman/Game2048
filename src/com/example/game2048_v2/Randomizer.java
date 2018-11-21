package com.example.game2048_v2;

import java.util.Random;

public class Randomizer {
	Random rand = new Random();

	private int ProbabilityOf4 = 1;
	private int ProbabilityOf2 = 9;

	public int GetRandomInt(int max) {
		int Val = rand.nextInt(max+1);
		return Val;
	}

	public int GetNewBlockValue() {
		int Val = rand.nextInt(ProbabilityOf2 + ProbabilityOf4);
		if (Val >= ProbabilityOf2) {
			Val = 4;
		} else {
			Val = 2;
		}
		return Val;
	}
}
