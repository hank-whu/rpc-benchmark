package benchmark.pool;

import stormpot.Poolable;
import stormpot.Slot;

public final class PoolableObject<T> implements Poolable, AutoCloseable {

	private final Slot slot;
	private final T value;

	PoolableObject(Slot slot, T value) {
		this.slot = slot;
		this.value = value;
	}

	public T get() {
		return value;
	}

	@Override
	public void release() {
		slot.release(this);
	}

	@Override
	public void close() throws Exception {
		release();
	}

}
