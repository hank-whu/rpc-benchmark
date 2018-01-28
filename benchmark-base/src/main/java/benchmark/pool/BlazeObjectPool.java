package benchmark.pool;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import stormpot.BlazePool;
import stormpot.Config;
import stormpot.PoolException;
import stormpot.Timeout;

/**
 * high performance objectpool<br>
 * https://github.com/chrisvest/object-pool-benchmarks
 * 
 * @author Hank
 *
 * @param <T>
 */
public class BlazeObjectPool<T> implements Closeable {
	private final BlazePool<PoolableObject<T>> blazePool;
	private final Timeout timeout = new Timeout(1, TimeUnit.MINUTES);

	public BlazeObjectPool(int poolSize, ObjectFactory<T> allocator) {
		Config<PoolableObject<T>> config = new Config<>();

		config.setAllocator(allocator);
		config.setSize(poolSize);
		config.setExpiration(new NoneExpiration<T>());

		blazePool = new BlazePool<>(config);
	}

	public PoolableObject<T> claim() throws PoolException, InterruptedException {
		return blazePool.claim(timeout);
	}

	@Override
	public void close() throws IOException {
		try {
			blazePool.shutdown().await(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
