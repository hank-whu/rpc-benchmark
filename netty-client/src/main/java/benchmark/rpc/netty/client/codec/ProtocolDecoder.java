package benchmark.rpc.netty.client.codec;

import benchmark.rpc.netty.serializer.FastestSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtocolDecoder extends LengthFieldBasedFrameDecoder {

	private static final int HEADER_FIELD_LENGTH = 4;

	public ProtocolDecoder(int maxFrameLength) {
		super(maxFrameLength, 0, HEADER_FIELD_LENGTH, 0, HEADER_FIELD_LENGTH);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf buffer = (ByteBuf) super.decode(ctx, in);

		if (buffer != null) {

			try {
				return FastestSerializer.readResponse(buffer);
			} finally {
				buffer.release();
			}
		}

		return null;
	}

}
