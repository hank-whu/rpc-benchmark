package benchmark.rpc.aeron.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TransferQueue;

import org.agrona.BitUtil;
import org.agrona.BufferUtil;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.io.DirectBufferOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Output;

import io.aeron.Aeron;
import io.aeron.Publication;

public class Publisher implements Closeable {
	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	private final static ThreadLocal<Kryo> kryoHolder = ThreadLocal.withInitial(() -> new Kryo());

	private final static ThreadLocal<UnsafeBuffer> bufferHolder = ThreadLocal.withInitial(() -> new UnsafeBuffer(
			BufferUtil.allocateDirectAligned(Config.MAX_MESSAGE_LENGTH, BitUtil.CACHE_LINE_LENGTH)));

	private volatile Aeron aeron;
	private volatile Publication publication;

	private final TransferQueue<Object> transferQueue = new LinkedTransferQueue<>();
	private Thread[] publishThreads;

	private volatile boolean isClosed;

	public Publisher() {
		super();
	}

	public void connect() {
		aeron = Aeron.connect();
		publication = aeron.addPublication(Config.PUBLISH_CHANNEL, Config.STREAM_ID);

		publishThreads = new Thread[NCPU];

		for (int i = 0; i < publishThreads.length; i++) {
			publishThreads[i] = new Thread(() -> {
				final IdleStrategy idleStrategy = new BackoffIdleStrategy(1000, 100, TimeUnit.MICROSECONDS.toNanos(1),
						TimeUnit.MICROSECONDS.toNanos(100));

				while (!isClosed) {
					try {
						doPublish(idleStrategy);
					} catch (InterruptedException e) {
						break;
					}
				}
			}, "subscribeThread-" + i);
		}

		for (int i = 0; i < publishThreads.length; i++) {
			publishThreads[i].start();
		}
	}

	@Override
	public void close() throws IOException {
		isClosed = true;

		for (Thread publishThread : publishThreads) {
			publishThread.interrupt();
		}
	}

	public void publish(Object obj, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		boolean result = transferQueue.tryTransfer(obj, timeout, unit);

		if (!result) {
			throw new TimeoutException();
		}
	}

	private void doPublish(IdleStrategy idleStrategy) throws InterruptedException {
		Object obj = transferQueue.take();

		final MutableDirectBuffer publishBuffer = bufferHolder.get();

		try (DirectBufferOutputStream outputStream = new DirectBufferOutputStream(publishBuffer, 0,
				Config.MAX_MESSAGE_LENGTH); //
				Output output = new FastOutput(outputStream)) {

			kryoHolder.get().writeObject(output, obj);
			output.flush();
			outputStream.flush();

			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				long result = publication.offer(publishBuffer, outputStream.offset(), outputStream.position());

				if (result > 0) {
					idleStrategy.reset();
					break;
				}

				if (result == Publication.BACK_PRESSURED || result == Publication.ADMIN_ACTION) {
					idleStrategy.idle();
					continue;
				}

				idleStrategy.reset();
				System.err.println("error to send, status code:" + result);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
