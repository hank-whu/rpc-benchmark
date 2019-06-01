package benchmark.rpc.netty.serializer;

import io.netty.buffer.ByteBuf;

public class LongSerializer implements Serializer<Long> {

	@Override
	public void write(ByteBuf byteBuf, Long value) {
		byteBuf.writeLong(value);
	}

	@Override
	public Long read(ByteBuf byteBuf) {
		return byteBuf.readLong();
	}

	@Override
	public Class<Long> typeClass() {
		return Long.class;
	}

}
