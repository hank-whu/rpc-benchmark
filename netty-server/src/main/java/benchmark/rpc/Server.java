package benchmark.rpc;

import java.net.InetSocketAddress;

import benchmark.rpc.netty.server.handler.BenchmarkChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import io.netty.util.concurrent.DefaultThreadFactory;

public class Server {

	public static final String host = "0.0.0.0";
	public static final int port = 8080;

	static {
		ResourceLeakDetector.setLevel(Level.DISABLED);
	}

	public static void main(String[] args) throws InterruptedException {
		if (Epoll.isAvailable()) {
			doRun(//
					new EpollEventLoopGroup(0, new DefaultThreadFactory(NioEventLoopGroup.class)), //
					EpollServerSocketChannel.class, //
					true);
		} else {
			doRun(//
					new NioEventLoopGroup(0, new DefaultThreadFactory(NioEventLoopGroup.class)), //
					NioServerSocketChannel.class, //
					false);
		}
	}

	private static void doRun(EventLoopGroup loupGroup, Class<? extends ServerChannel> serverChannelClass,
			boolean isEpoll) throws InterruptedException {
		try {
			InetSocketAddress inet = new InetSocketAddress(port);

			ServerBootstrap b = new ServerBootstrap();

			if (isEpoll) {
				b.option(EpollChannelOption.SO_REUSEPORT, true);
			}

			b.option(ChannelOption.SO_BACKLOG, 1024 * 8);
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.group(loupGroup).channel(serverChannelClass).childHandler(new BenchmarkChannelInitializer());
			b.childOption(ChannelOption.SO_REUSEADDR, true);

			Channel ch = b.bind(inet).sync().channel();

			System.out.printf("Httpd started. Listening on: %s%n", inet.toString());

			ch.closeFuture().sync();
		} finally {
			loupGroup.shutdownGracefully().sync();
		}
	}

}
