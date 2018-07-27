package benchmark.pool;

import java.util.concurrent.locks.LockSupport;

public final class WaitStrategy {

	public final int idle(final int idleCounter) {

		final int idled = idleCounter + 1;

		if (idleCounter < 10) {
			Thread.onSpinWait();
			return idled;
		}

		if (idleCounter < 10 + 10) {
			Thread.yield();
			return idled;
		}

		LockSupport.parkNanos(1L);

		return idled;
	}

}
