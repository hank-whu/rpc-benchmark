package benchmark.rpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.ShutdownSignalBarrier;

import benchmark.rpc.aeron.server.Publisher;
import benchmark.rpc.aeron.server.RequestResponseService;
import benchmark.rpc.aeron.server.Subscriber;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

public class Server {

	private static final MediaDriver.Context ctx = new MediaDriver.Context()//
			.termBufferSparseFile(false)//
			.threadingMode(ThreadingMode.DEDICATED)//
			.conductorIdleStrategy(new BackoffIdleStrategy(1000, 100, TimeUnit.MICROSECONDS.toNanos(1),
					TimeUnit.MICROSECONDS.toNanos(100)))//
			.receiverIdleStrategy(new BackoffIdleStrategy(1000, 100, TimeUnit.MICROSECONDS.toNanos(1),
					TimeUnit.MICROSECONDS.toNanos(100)))//
			.senderIdleStrategy(new BackoffIdleStrategy(1000, 100, TimeUnit.MICROSECONDS.toNanos(1),
					TimeUnit.MICROSECONDS.toNanos(100)));

	public static void main(String[] args) throws IOException, InterruptedException {
		try (MediaDriver mediaDriver = MediaDriver.launch(ctx);
				Publisher publisher = new Publisher();
				Subscriber subscriber = new Subscriber(new RequestResponseService(publisher));) {

			publisher.connect();
			subscriber.connect();

			new ShutdownSignalBarrier().await();
			System.out.println("Shutdown Driver...");
		}
	}

}
