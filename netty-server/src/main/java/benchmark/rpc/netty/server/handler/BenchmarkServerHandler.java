package benchmark.rpc.netty.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmark.rpc.protocol.Request;
import benchmark.rpc.protocol.Response;
import benchmark.rpc.route.RouteService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BenchmarkServerHandler extends SimpleChannelInboundHandler<Request> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkServerHandler.class);

	private final RouteService routeService = new RouteService();

	protected void channelRead0(ChannelHandlerContext ctx, Request msg) throws Exception {
		final long requestId = msg.getRequestId();
		final int serviceId = msg.getServiceId();
		final Object[] params = msg.getParams();

		Object result = routeService.invoke(serviceId, params);

		Response response = new Response();

		response.setRequestId(requestId);
		response.setStatusCode((byte) 1);
		response.setResult(result);

		ctx.writeAndFlush(response, ctx.voidPromise());
	}
	
	

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("Exception caught on {}, ", ctx.channel(), cause);
		ctx.channel().close();
	}
}
