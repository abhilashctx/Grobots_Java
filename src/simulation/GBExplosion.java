/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;

import exception.GBSimulationError;
import sides.Side;
import support.FinePoint;
import support.GBObjectClass;

public class GBExplosion extends GBTimedShot {
	// public:
	public GBExplosion(FinePoint where, Side who, double howMuch) {
		super(where, PowerRadius(howMuch), who, howMuch, kExplosionLifetime);
		if (howMuch < 0)
			throw new GBSimulationError("negative explosion");
		// TODO: when sound is done
		/*
		 * if ( howMuch > 100 ) StartSound(siLargeExplosion); else if ( howMuch
		 * > 30 ) StartSound(siMediumExplosion); else if ( howMuch > 10 )
		 * StartSound(siSmallExplosion); else StartSound(siTinyExplosion);
		 */
	}

	@Override
	public GBObjectClass Class() {
		if (lifetime > 0)
			return GBObjectClass.ocArea;
		else
			return GBObjectClass.ocDead;
	}

	@Override
	public void CollideWith(GBObject other) {
		if (lifetime < kExplosionLifetime)
			return;
		double oMass = Math.max(other.Mass(), kExplosionMinEffectiveMass);
		if (oMass == 0)
			return; // massless objects get 0 damage
		double damage = power
				/ (Math.pow(power / oMass, kExplosionDamageMassExponent)
						* kLargeExplosionIneffectiveness + 1);
		other.TakeDamage(damage * OverlapFraction(other), owner);
		Push(other,
				damage
						* other.OverlapFraction(this)
						* (other.Class() == GBObjectClass.ocFood ? kExplosionFoodPushRatio
								: kExplosionPushRatio));
	}

	@Override
	public void Act(GBWorld world) {
		super.Act(world);
		if (lifetime == 0) {
			int maxLifetime = (int) (Math.floor(Math.sqrt(power)) * kExplosionSmokeLifetimeFactor);
			for (int i = (int) Math.round(Math.max(power
					* kExplosionSmokesPerPower, kExplosionMinSmokes)); i > 0; i--)
				world.addObjectLater(new GBSmoke(Position().add(
						world.random.Vector(Radius())), world.random
						.Vector(GBTimedDecoration.kSmokeMaxSpeed), world
						.random.intInRange(
								GBTimedDecoration.kSmokeMinLifetime,
								maxLifetime)));
		}
	}

	@Override
	public Color Color() {
		return new Color(1, 0.9f, 0.2f);
	}

	@Override
	public void Draw(Graphics g, GBProjection proj, boolean detailed) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle where = getScreenRect(proj);
		g2d.setColor(Color());
		g2d.fillOval(where.x, where.y, where.width, where.height);
	}

	public static final double PowerRadius(double pwr) {
		return Math.pow(pwr, kExplosionRadiusExponent) * kExplosionRadiusRatio;
	}

}
