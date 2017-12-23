package benchmark.rpc.netty.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmark.rpc.netty.client.future.FutureContainer;
import benchmark.rpc.protocol.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BenchmarkClientHandler extends SimpleChannelInboundHandler<Response> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkClientHandler.class);

	private final FutureContainer futureContainer;

	public BenchmarkClientHandler(FutureContainer futureContainer) {
		this.futureContainer = futureContainer;
	}

	protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
		futureContainer.notifyResponse(response);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("Exception caught on {}, ", ctx.channel(), cause);
		ctx.channel().close();
	}

}
