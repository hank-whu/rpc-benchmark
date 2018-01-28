package benchmark.pool;

import java.io.Closeable;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @author Hank
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class LockObjectPool<T> implements Closeable {

	static class ObjectWithLock {
		public final Object obj;
		public final Lock lock;

		public ObjectWithLock(Object obj) {
			this.obj = obj;
			this.lock = new ReentrantLock();
		}
	}

	private final ObjectWithLock[] array;
	private final IdentityHashMap<Object, ObjectWithLock> lockMap;
	private final int poolSize;

	public LockObjectPool(int poolSize, Supplier<T> producer) {
		this.poolSize = poolSize;

		lockMap = new IdentityHashMap<>(poolSize * 2);

		array = new ObjectWithLock[poolSize];
		for (int i = 0; i < poolSize; i++) {
			T t = producer.get();
			ObjectWithLock objectWithLock = new ObjectWithLock(t);

			array[i] = objectWithLock;
			lockMap.put(t, objectWithLock);
		}
	}

	public T borrow() {
		int index = ThreadLocalRandom.current().nextInt(poolSize);
		ObjectWithLock objectWithLock = array[index];

		objectWithLock.lock.lock();

		return (T) objectWithLock.obj;
	}

	public void release(T t) {
		if (t == null) {
			return;
		}

		lockMap.get(t).lock.unlock();
	}

	@Override
	public void close() throws IOException {

		for (int i = 0; i < array.length; i++) {
			Object obj = array[i].obj;

			if (obj instanceof AutoCloseable) {
				try {
					((AutoCloseable) obj).close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
