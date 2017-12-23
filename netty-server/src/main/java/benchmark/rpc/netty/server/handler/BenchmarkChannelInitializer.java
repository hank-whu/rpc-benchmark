package benchmark.rpc.netty.server.handler;

import benchmark.rpc.netty.server.codec.ProtocolDecoder;
import benchmark.rpc.netty.server.codec.ProtocolEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class BenchmarkChannelInitializer extends ChannelInitializer<SocketChannel> {

	public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline()//
				.addLast("encoder", new ProtocolEncoder())//
				.addLast("decoder", new ProtocolDecoder(MAX_FRAME_LENGTH))//
				.addLast("handler", new BenchmarkServerHandler());
	}
}
