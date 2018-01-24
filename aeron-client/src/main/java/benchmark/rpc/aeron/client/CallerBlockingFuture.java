package benchmark.rpc.aeron.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * only for benchmark.rpc.netty.client, do not use it, it is not safe
 * 
 * @author Hank
 *
 * @param <V>
 */
final class CallerBlockingFuture<V> implements Future<V> {

	private final Thread caller = Thread.currentThread();

	private volatile V value = null;
	private volatile boolean isParked = false;
	private volatile boolean isDone = false;

	public void setValue(V value) {
		this.value = value;
		isDone = true;

		if (isParked) {
			LockSupport.unpark(caller);
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCancelled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (isDone) {
			return value;
		}

		if (!isDone) {
			isParked = true;
			LockSupport.parkNanos(unit.toNanos(timeout) - TimeUnit.MILLISECONDS.toNanos(50));
			isParked = false;
		}

		return value;
	}

}
