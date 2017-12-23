package benchmark.rpc.netty.client.codec;

import benchmark.rpc.netty.serializer.ProtostuffSerializer;
import benchmark.rpc.protocol.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtocolEncoder extends MessageToByteEncoder<Request> {

	protected void encode(ChannelHandlerContext ctx, Request request, ByteBuf buffer) throws Exception {
		ProtostuffSerializer.writeRequest(buffer, request);
	}
}
