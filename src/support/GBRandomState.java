/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package support;



//Modified to use java's built-in Random class

public class GBRandomState {
	int seed;
	java.util.Random rnd;

	// convenient global generator
	public static GBRandomState gRandoms = new GBRandomState();

	public GBRandomState() {
		rnd = new java.util.Random();
		seed = rnd.nextInt();
	}

	public GBRandomState(int newseed) {
		seed = newseed;
	}

	public int Generateint() {
		return rnd.nextInt();
	}

	public short GenerateShort() {
		return (short) (Generateint() >> 16);
	}

	public int intInRange(int min, int max) {
		if (min == max)
			return min;
		else if (min > max)
			// With this limitation we can't generate negative numbers, but
			// changing it might change robot behavior
			return intInRange(max, min);
		int result = rnd.nextInt(max - min + 1) + min;
		return result;
	}

	public double InRange(double min, double max) {
		return rnd.nextDouble() * (max - min) + min;
	}

	public float FloatInRange(float min, float max) {
		return rnd.nextFloat() * (max - min) + min;
	}

	public double Angle() {
		return InRange(GBMath.kEpsilon - GBMath.kPi, GBMath.kPi);
	}

	public FinePoint Vector(double maxLength) {
		return FinePoint.makePolar(InRange(0, maxLength), Angle());
	}

	public GBColor Color() {
		return new GBColor(FloatInRange(0, 1), FloatInRange(0, 1),
				FloatInRange(0, 1));
	}

	// TODO: This was marked as broken. Fixed syntax but was there more to it?
	public GBColor ColorNear(GBColor old, float dist) {
		return new GBColor(FloatInRange(old.r - dist, old.r + dist),
				FloatInRange(old.g - dist, old.g + dist), FloatInRange(old.b
						- dist, old.b + dist));
	}

	public boolean bool(double probability) {
		return InRange(0, 1 - GBMath.kEpsilon) < probability;
	}

	public boolean bool(int num, int denom) {
		return intInRange(0, denom - 1) < num;
	}

	public int GetSeed() {
		return seed;
	}

	public void SetSeed(int newSeed) {
		seed = newSeed;
	}
}