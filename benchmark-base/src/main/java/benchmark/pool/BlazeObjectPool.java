package benchmark.pool;

import static benchmark.pool.UnsafeUtils.unsafe;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Hank
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class BlazeObjectPool<T> implements Closeable {

	private static final long IDX;
	private static final int ABASE;
	private static final int ASHIFT;
	private static final Object BORROWED = new Object();
	private static final WaitStrategy WAIT_STRATEGY = new WaitStrategy();

	private final int poolSize;

	volatile long p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17;
	private volatile int idx;

	volatile long q0, q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, q11, q12, q13, q14, q15, q16, q17;
	private final Object[] array;

	private final Object[] closeList;
	volatile long r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17;

	private volatile boolean isClosing = false;

	public BlazeObjectPool(int poolSize, Supplier<T> producer) {
		this.poolSize = poolSize;
		idx = poolSize - 1;

		closeList = new Object[poolSize];
		array = new Object[poolSize];
		for (int i = 0; i < poolSize; i++) {
			T t = producer.get();

			array[i] = t;
			closeList[i] = t;
		}
	}

	public T borrow() {
		for (int i = 0; i < Integer.MAX_VALUE; i++) {

			if (isClosing) {
				return null;
			}

			if (idx < 0) {
				WAIT_STRATEGY.idle(i);
				continue;
			}

			int index = unsafe().getAndAddInt(this, IDX, -1);

			if (index < 0) {
				unsafe().getAndAddInt(this, IDX, 1);
				WAIT_STRATEGY.idle(i);
				continue;
			}

			Object obj = unsafe().getAndSetObject(array, offset(index), BORROWED);

			if (obj != BORROWED) {
				return (T) obj;
			}

			unsafe().getAndAddInt(this, IDX, 1);
			WAIT_STRATEGY.idle(i);
		}

		return null;
	}

	public void release(T t) {
		if (t == null) {
			return;
		}

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			if (isClosing) {
				return;
			}

			if (idx < -1 || idx > poolSize - 2) {
				WAIT_STRATEGY.idle(i);
				continue;
			}

			int index = unsafe().getAndAddInt(this, IDX, 1) + 1;

			if (index < 0 || index >= poolSize) {
				unsafe().getAndAddInt(this, IDX, -1);
				WAIT_STRATEGY.idle(i);
				continue;
			}

			boolean success = unsafe().compareAndSwapObject(array, offset(index), BORROWED, t);

			if (success) {
				return;
			}

			unsafe().getAndAddInt(this, IDX, -1);
			WAIT_STRATEGY.idle(i);
		}
	}

	@Override
	public void close() throws IOException {
		isClosing = true;

		for (int i = 0; i < 1000; i++) {
			if (idx == poolSize - 1) {
				break;
			}

			try {
				TimeUnit.MILLISECONDS.sleep(15);
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}

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
			IDX = unsafe().objectFieldOffset(BlazeObjectPool.class.getDeclaredField("idx"));

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
