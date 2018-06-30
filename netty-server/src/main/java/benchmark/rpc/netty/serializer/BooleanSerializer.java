package benchmark.rpc.netty.serializer;

import io.netty.buffer.ByteBuf;

public class BooleanSerializer implements Serializer<Boolean> {

	@Override
	public void write(ByteBuf byteBuf, Boolean value) {
		byteBuf.writeBoolean(value);
	}

	@Override
	public Boolean read(ByteBuf byteBuf) {
		return byteBuf.readBoolean();
	}

	@Override
	public Class<Boolean> typeClass() {
		return Boolean.class;
	}

}
