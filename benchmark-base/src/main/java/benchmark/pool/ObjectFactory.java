package benchmark.pool;

import stormpot.Allocator;
import stormpot.Slot;

@FunctionalInterface
public interface ObjectFactory<T> extends Allocator<PoolableObject<T>> {

	public T newInstance();

	@Override
	default public PoolableObject<T> allocate(Slot slot) throws Exception {
		return new PoolableObject<T>(slot, newInstance());
	}

	@Override
	default public void deallocate(PoolableObject<T> poolable) throws Exception {
		if (poolable == null) {
			return;
		}

		T t = poolable.get();

		if (t == null) {
			return;
		}

		if (t instanceof AutoCloseable) {
			((AutoCloseable) t).close();
		}
	}

}
