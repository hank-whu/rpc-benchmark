package benchmark.pool;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Supplier;

import org.vibur.objectpool.ConcurrentPool;
import org.vibur.objectpool.PoolObjectFactory;
import org.vibur.objectpool.PoolService;
import org.vibur.objectpool.util.ConcurrentCollection;
import org.vibur.objectpool.util.ConcurrentLinkedQueueCollection;

public class ViberObjectPool<T> implements Closeable {

	private final PoolService<T> pool;

	public ViberObjectPool(int poolSize, Supplier<T> producer) {
		PoolObjectFactory<T> poolObjectFactory = new PoolObjectFactory<T>() {

			@Override
			public T create() {
				return producer.get();
			}

			@Override
			public boolean readyToTake(T obj) {
				return true;
			}

			@Override
			public boolean readyToRestore(T obj) {
				return true;
			}

			@Override
			public void destroy(T obj) {
				if (obj instanceof AutoCloseable) {
					try {
						((AutoCloseable) obj).close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};

		ConcurrentCollection<T> concurrentCollection = new ConcurrentLinkedQueueCollection<>();
		pool = new ConcurrentPool<>(concurrentCollection, poolObjectFactory, poolSize, poolSize, false);
	}

	public T borrow() {
		return pool.take();
	}

	public void release(T t) {
		if (t == null) {
			return;
		}

		pool.restore(t);
	}

	@Override
	public void close() throws IOException {
		pool.close();
	}

}