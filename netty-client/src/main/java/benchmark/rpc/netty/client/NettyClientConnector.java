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
import benchmark.rpc.protocol.Request;
import benchmark.rpc.protocol.Response;
import io.netty.bootstrap.Bootstrap;
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

public class NettyClientConnector implements Closeable {
	static {
		ResourceLeakDetector.setLevel(Level.DISABLED);
	}

	public static final int CONNECT_COUNT = 1;

	private final FutureContainer futureContainer = new FutureContainer();

	private final String host;
	private final int port;
	private long defaultTimeout = 15_000L;
	private EventLoopGroup eventLoopGroup;
	private final Channel[] channels = new Channel[CONNECT_COUNT];

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

		futureContainer.addFuture(requestId, future);

		try {
			channels[ThreadLocalRandom.current().nextInt(CONNECT_COUNT)].writeAndFlush(request);
			return future.get();
		} finally {
			futureContainer.remove(requestId);
		}
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

		for (int i = 0; i < channels.length; i++) {
			channels[i] = bootstrap.connect(host, port).sync().channel();
		}
	}

}
