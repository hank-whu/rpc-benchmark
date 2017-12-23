package benchmark.rpc.netty.client.handler;

import benchmark.rpc.netty.client.codec.ProtocolDecoder;
import benchmark.rpc.netty.client.codec.ProtocolEncoder;
import benchmark.rpc.netty.client.future.FutureContainer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class BenchmarkChannelInitializer extends ChannelInitializer<SocketChannel> {

	public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

	private final FutureContainer futureContainer;

	public BenchmarkChannelInitializer(FutureContainer futureContainer) {
		this.futureContainer = futureContainer;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline()//
				.addLast("encoder", new ProtocolEncoder())//
				.addLast("decoder", new ProtocolDecoder(MAX_FRAME_LENGTH))//
				.addLast("handler", new BenchmarkClientHandler(futureContainer));
	}
}
