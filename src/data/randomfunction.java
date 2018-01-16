package data;

import java.util.Random;

public class randomfunction {
	public int uniformRandom(int min_value, int max_value, Random r) {
		int interval = max_value - min_value + 1;
		int x = r.nextInt(interval);
		return min_value + x;
	}

	public double random_uniform_0_1(Random r) {
		return r.nextDouble();
	}

	public double expdev(Random r) {
		double dum;
		dum = this.random_uniform_0_1(r);
		return (-Math.log(dum));
	}
}
