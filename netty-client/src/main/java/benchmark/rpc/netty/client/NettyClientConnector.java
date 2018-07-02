package benchmark.rpc.netty.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import benchmark.rpc.netty.client.future.FutureContainer;
import benchmark.rpc.netty.client.handler.BenchmarkChannelInitializer;
import benchmark.rpc.netty.serializer.FastestSerializer;
import benchmark.rpc.protocol.Request;
import benchmark.rpc.protocol.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscGrowableAtomicArrayQueue;

public class NettyClientConnector implements Closeable {
	static {
		ResourceLeakDetector.setLevel(Level.DISABLED);
	}

	public static final int CONNECT_COUNT = 4;

	private final FutureContainer futureContainer = new FutureContainer();

	private final String host;
	private final int port;
	private long defaultTimeout = 15_000L;
	private EventLoopGroup eventLoopGroup;
	private final Channel[] channels = new Channel[CONNECT_COUNT];
	@SuppressWarnings("unchecked")
	private final MpscGrowableAtomicArrayQueue<Request>[] queues = new MpscGrowableAtomicArrayQueue[CONNECT_COUNT];

	public NettyClientConnector(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void setDefaultTimeout(long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	public Response execute(Request request) throws InterruptedException, ExecutionException, TimeoutException {
		return execute(request, defaultTimeout, TimeUnit.MILLISECONDS);
	}

	public Response execute(Request request, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {

		final long requestId = request.getRequestId();
		final CompletableFuture<Response> future = new CompletableFuture<>();

		try {
			futureContainer.addFuture(requestId, future);

			int index = ThreadLocalRandom.current().nextInt(CONNECT_COUNT);
			Channel channel = channels[index];
			MpscGrowableAtomicArrayQueue<Request> queue = queues[index];

			while (!queue.offer(request)) {
				batchSend(channel, queue);
			}

			batchSend(channel, queue);

			return future.get();
		} finally {
			futureContainer.remove(requestId);
		}
	}

	private void batchSend(Channel channel, MpscGrowableAtomicArrayQueue<Request> queue) {
		if (queue.isEmpty()) {
			return;
		}

		channel.eventLoop().execute(() -> {
			if (queue.isEmpty()) {
				return;
			}

			ByteBuf byteBuf = channel.alloc().ioBuffer(1024 * 4);

			while (true) {
				Request request = queue.poll();

				if (request == null) {
					break;
				}

				try {
					FastestSerializer.writeRequest(byteBuf, request);
				} catch (Throwable t) {
					t.printStackTrace();
					channel.close();
				}
			}

			if (byteBuf.isReadable()) {
				channel.writeAndFlush(byteBuf, channel.voidPromise());
			} else {
				byteBuf.release();
			}
		});
	}

	@Override
	public void close() throws IOException {
		for (int i = 0; i < channels.length; i++) {
			channels[i].close();
		}

		if (eventLoopGroup != null) {
			eventLoopGroup.shutdownGracefully();
		}

	}

	public void connect() throws InterruptedException {

		if (Epoll.isAvailable()) {
			if (eventLoopGroup == null) {
				eventLoopGroup = new EpollEventLoopGroup(0, new DefaultThreadFactory(NioEventLoopGroup.class));
			}

			doConnect(eventLoopGroup, EpollSocketChannel.class, true);
		} else {
			if (eventLoopGroup == null) {
				eventLoopGroup = new NioEventLoopGroup(0, new DefaultThreadFactory(NioEventLoopGroup.class));
			}

			doConnect(eventLoopGroup, NioSocketChannel.class, false);
		}
	}

	private void doConnect(EventLoopGroup loupGroup, Class<? extends SocketChannel> serverChannelClass, boolean isEpoll)
			throws InterruptedException {

		final Bootstrap bootstrap = new Bootstrap();

		if (isEpoll) {
			bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
		}

		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.option(ChannelOption.SO_RCVBUF, 256 * 1024);
		bootstrap.option(ChannelOption.SO_SNDBUF, 256 * 1024);
		bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, //
				new WriteBufferWaterMark(1024 * 1024, 2048 * 1024));

		bootstrap.group(loupGroup);
		bootstrap.channel(serverChannelClass);
		bootstrap.handler(new BenchmarkChannelInitializer(futureContainer));

		for (int i = 0; i < CONNECT_COUNT; i++) {
			channels[i] = bootstrap.connect(host, port).sync().channel();
			queues[i] = new MpscGrowableAtomicArrayQueue<>(4 * 1024);
		}
	}

}
