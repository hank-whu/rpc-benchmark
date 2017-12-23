package benchmark.rpc.netty.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

	private final FutureContainer futureContainer = new FutureContainer();

	private final String host;
	private final int port;
	private volatile long defaultTimeout = 15_000L;
	private volatile EventLoopGroup eventLoopGroup;
	private volatile Channel channel;

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
			channel.writeAndFlush(request);
			return future.get(timeout, unit);
		} finally {
			futureContainer.remove(requestId);
		}
	}

	@Override
	public void close() throws IOException {
		if (channel != null) {
			channel.close();
		}

		if (eventLoopGroup != null) {
			eventLoopGroup.shutdownGracefully(1, 1, TimeUnit.MILLISECONDS);
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

		// bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);

		bootstrap.group(loupGroup);
		bootstrap.channel(serverChannelClass);
		bootstrap.handler(new BenchmarkChannelInitializer(futureContainer));

		channel = bootstrap.connect(host, port).sync().channel();
	}

}
