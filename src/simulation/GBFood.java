/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import sides.Side;
import support.FinePoint;
import exception.GBSimulationError;

public class GBFood extends GBObject {
	public static final double kFoodMinRadius = 0.1;
	public static final double kFoodRadiusFactor = 0.01;
	public static final double kFoodMassPerValue = 0.015;
	public static final double kFoodDecayRate = 0.12;

	public static final double kFriction = 0.004;
	public static final double kLinearDragFactor = 0.01;
	public static final double kQuadraticDragFactor = 0.3;
	protected double value;

	protected void Recalculate() {
		radius = Math.sqrt(value) * kFoodRadiusFactor + kFoodMinRadius;
		mass = value * kFoodMassPerValue;
	}

	public GBFood(FinePoint where, double val) {
		super(where, Math.sqrt(val) * kFoodRadiusFactor + kFoodMinRadius, val
				* kFoodMassPerValue);
		value = Math.max(val, 0);
		makeImage();
	}

	public GBFood(FinePoint where, FinePoint vel, double val) {
		super(where, Math.sqrt(val) * kFoodRadiusFactor + kFoodMinRadius, vel,
				val * kFoodMassPerValue);
		value = val;
		makeImage();

		if (val < 0)
			throw new GBSimulationError("negative-value food");
	}

	void makeImage() {
		image = new BufferedImage(21, 21, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setColor(Color());
		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
		g2d.dispose();
	}

	@Override
	public double Energy() {
		return value;
	}

	@Override
	public double TakeEnergy(double limit) {
		if (limit < 0)
			throw new GBSimulationError("can't take negative energy from food");
		if (value <= limit) {
			double amt = value;
			value = 0;
			Recalculate();
			return amt;
		} else {
			value -= limit;
			if (value < 0)
				throw new GBSimulationError("taking negative energy from food");
			Recalculate();
			return limit;
		}
	}

	@Override
	public double MaxTakeEnergy() {
		return value;
	}

	@Override
	public GBObjectClass Class() {
		if (value > 0)
			return GBObjectClass.ocFood;
		else
			return GBObjectClass.ocDead;
	}

	@Override
	public Side Owner() {
		return null;
	}

	@Override
	public void Move() {
		super.Move();
		Drag(kFriction, kLinearDragFactor, kQuadraticDragFactor);
	}

	@Override
	public void Act(GBWorld world) {
		value = Math.max(value - kFoodDecayRate, 0);
		Recalculate(); // FIXME this is slow
	}

	@Override
	public Color Color() {
		return Color.white;
	}

	@Override
	public void Draw(Graphics g, GBProjection<GBObject> proj, boolean detailed) {
		drawImage(g, proj);
	}
};
