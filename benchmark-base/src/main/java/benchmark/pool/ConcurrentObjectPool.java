package benchmark.pool;

import static benchmark.pool.UnsafeUtils.unsafe;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author Hank
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class ConcurrentObjectPool<T> implements Closeable {

	private static final long EXCHANGE;
	private static final int ABASE;
	private static final int ASHIFT;

	private static final Object EMPTY = new Object();
	private static final WaitStrategy WAIT_STRATEGY = new WaitStrategy();

	private final int size;

	volatile long p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17;
	private final Object[] array;

	private final Object[] closeList;
	volatile long q0, q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, q11, q12, q13, q14, q15, q16, q17;

	private volatile Object exchange = EMPTY;
	volatile long r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17;

	public ConcurrentObjectPool(int poolSize, Supplier<T> producer) {
		this.size = poolSize;
		this.closeList = new Object[poolSize];
		this.array = new Object[poolSize];

		for (int i = 0; i < poolSize; i++) {
			T t = producer.get();

			array[i] = t;
			closeList[i] = t;
		}
	}

	public T borrow() {
		T fast = exchange();
		if (fast != null) {// 抢到了
			return fast;
		}

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			int random = ThreadLocalRandom.current().nextInt(size);

			for (int j = 0; j < size; j++) {
				long offset = offset((random + j) % size);
				Object obj = unsafe().getObjectVolatile(array, offset);

				if (obj != EMPTY) {
					if (unsafe().compareAndSwapObject(array, offset, obj, EMPTY)) {
						return (T) obj;
					} else {
						break;
					}
				}
			}

			WAIT_STRATEGY.idle(i);
		}

		return null;
	}

	public void release(final T t) {
		if (t == null) {
			return;
		}

		final boolean exchanged = exchange(t);

		for (int i = 0; i < Integer.MAX_VALUE; i++) {

			int random = ThreadLocalRandom.current().nextInt(size);

			for (int j = 0; j < size; j++) {
				if (exchanged && !checkExchange(t)) {
					return;
				}

				long offset = offset((random + j) % size);
				Object obj = unsafe().getObjectVolatile(array, offset);

				if (obj == EMPTY) {
					if (exchanged && !cancelExchange(t)) {// 被其他线程抢走了
						return;
					}

					if (unsafe().compareAndSwapObject(array, offset, obj, t)) {// 归还资源
						return;
					}

					break;
				}
			}

			WAIT_STRATEGY.idle(i);
		}
	}

	private boolean exchange(final Object value) {
		return unsafe().compareAndSwapObject(this, EXCHANGE, EMPTY, value);
	}

	private boolean cancelExchange(final Object value) {
		return unsafe().compareAndSwapObject(this, EXCHANGE, value, EMPTY);
	}

	private boolean checkExchange(final Object value) {
		return value == exchange;
	}

	private T exchange() {
		Object fast = exchange;

		// 抢一下
		if (fast != EMPTY && unsafe().compareAndSwapObject(this, EXCHANGE, fast, EMPTY)) {
			return (T) fast;// 抢到了
		}

		// 没抢到
		return null;
	}

	@Override
	public void close() throws IOException {
		for (int i = 0; i < closeList.length; i++) {
			Object obj = closeList[i];

			if (obj instanceof AutoCloseable) {
				try {
					((AutoCloseable) obj).close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final long offset(int key) {
		return ((long) key << ASHIFT) + ABASE;
	}

	static {
		try {
			EXCHANGE = unsafe().objectFieldOffset(ConcurrentObjectPool.class.getDeclaredField("exchange"));

			ABASE = unsafe().arrayBaseOffset(Object[].class);

			int scale = unsafe().arrayIndexScale(Object[].class);
			if ((scale & (scale - 1)) != 0) {
				throw new Error("array index scale not a power of two");
			}

			ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
		} catch (Exception e) {
			throw new Error(e);
		}
	}
}
