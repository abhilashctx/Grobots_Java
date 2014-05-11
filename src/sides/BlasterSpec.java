package sides;

public class BlasterSpec implements Hardware<BlasterSpec> {

	@Override
	public double Mass() {
		if (damage > 0)
			return (1.0 / reloadTime + HardwareSpec.kBlasterBarrel)
					* (damage + HardwareSpec.kBlasterDamageOverhead)
					* (HardwareSpec.kBlasterMassPerDamageRate + range
							* HardwareSpec.kBlasterMassPerRange + Math.pow(
							range, 2)
							* HardwareSpec.kBlasterMassPerRangeSquared);
		else
			return 0;
	}

	@Override
	public double Cost() {
		if (damage > 0)
			return (1.0 / reloadTime + HardwareSpec.kBlasterBarrel)
					* (damage + HardwareSpec.kBlasterDamageOverhead)
					* (HardwareSpec.kBlasterCostPerDamageRate + range
							* HardwareSpec.kBlasterCostPerRange + Math.pow(
							range, 2)
							* HardwareSpec.kBlasterCostPerRangeSquared);
		else
			return 0;
	}

	@Override
	public BlasterSpec clone() {
		BlasterSpec ret = new BlasterSpec();
		ret.damage = damage;
		ret.range = range;
		ret.speed = speed;
		ret.lifetime = lifetime;
		ret.reloadTime = reloadTime;
		return ret;
	}

	public BlasterSpec() {
		damage = 0;
		range = 0;
		speed = 0;
		lifetime = 0;
		reloadTime = 1;
	}

	private double damage;
	private double speed;
	private double range;
	private long reloadTime;
	private long lifetime;

	public double Damage() {
		return damage;
	}

	public double Range() {
		return range;
	}

	public double Speed() {
		return speed;
	}

	public long Lifetime() {
		return lifetime;
	}

	public long ReloadTime() {
		return reloadTime;
	}

	public void Set(double dmg, double rng, long reload) {
		damage = Math.max(dmg, 0);
		range = Math.max(rng, 0);
		speed = Math.pow(range / HardwareSpec.kBlasterLifetimeSpeedTradeoff,
				HardwareSpec.kBlasterSpeedExponent);
		lifetime = (long) Math.ceil(range / speed);
		reloadTime = (reload < 1) ? 1 : reload; // limit to >= 1
	}

	public double FiringCost() {
		return (damage + HardwareSpec.kBlasterDamageOverhead)
				* (HardwareSpec.kBlasterFiringCostPerDamage + range
						* HardwareSpec.kBlasterFiringCostPerRange + Math.pow(
						range, 2)
						* HardwareSpec.kBlasterFiringCostPerRangeSquared);
	}
}
