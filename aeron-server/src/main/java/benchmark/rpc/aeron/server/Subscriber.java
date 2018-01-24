package benchmark.rpc.aeron.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TransferQueue;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.io.DirectBufferInputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.Input;

import benchmark.rpc.protocol.Request;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;

public class Subscriber implements FragmentHandler, Closeable {
	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	private final RequestCallback requestCallback;
	private final TransferQueue<Object> transferQueue = new LinkedTransferQueue<>();

	private boolean isClosed;

	private volatile Aeron aeron;
	private volatile Subscription subscription;
	private volatile Thread[] subscribeThreads;

	// Maximum number of message fragments to receive during a single 'poll'
	// operation
	private final int fragmentLimitCount = 32;
	private final static ThreadLocal<Kryo> kryoHolder = ThreadLocal.withInitial(() -> new Kryo());

	public Subscriber(RequestCallback requestCallback) {
		this.requestCallback = requestCallback;
	}

	public void connect() {
		aeron = Aeron.connect();
		subscription = aeron.addSubscription(Config.SUBSCRIBE_CHANNEL, Config.STREAM_ID);

		subscribeThreads = new Thread[NCPU];

		for (int i = 0; i < subscribeThreads.length; i++) {
			subscribeThreads[i] = new Thread(() -> {
				final IdleStrategy idleStrategy = new BackoffIdleStrategy(1000, 100, TimeUnit.MICROSECONDS.toNanos(1),
						TimeUnit.MICROSECONDS.toNanos(100));

				while (!isClosed) {
					try {
						doRead(idleStrategy);
					} catch (InterruptedException e) {
						break;
					}
				}
			}, "subscribeThread-" + i);
		}

		for (int i = 0; i < subscribeThreads.length; i++) {
			subscribeThreads[i].start();
		}
	}

	@Override
	public void close() throws IOException {
		isClosed = true;

		for (Thread subscribeThread : subscribeThreads) {
			subscribeThread.interrupt();
		}
	}

	public void publish(Object obj, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		boolean result = transferQueue.tryTransfer(obj, timeout, unit);

		if (!result) {
			throw new TimeoutException();
		}
	}

	private void doRead(IdleStrategy idleStrategy) throws InterruptedException {
		// poll delivers messages to the dataHandler as they arrive
		// and returns number of fragments read, or 0
		// if no data is available.
		final int fragmentsRead = subscription.poll(this, fragmentLimitCount);

		// Give the IdleStrategy a chance to spin/yield/sleep to reduce CPU
		// use if no messages were received.
		idleStrategy.idle(fragmentsRead);
	}

	@Override
	public void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
		try (InputStream inputStream = new DirectBufferInputStream(buffer, offset, length);
				Input input = new FastInput(inputStream)) {
			Request request = kryoHolder.get().readObject(input, Request.class);
			requestCallback.onRequest(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
