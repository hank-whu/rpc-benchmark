package benchmark.rpc.netty.serializer;

import io.netty.buffer.ByteBuf;

public class VoidSerializer implements Serializer<Void> {

	@Override
	public void write(ByteBuf byteBuf, Void value) {
	}

	@Override
	public Void read(ByteBuf byteBuf) {
		return null;
	}

	@Override
	public Class<Void> typeClass() {
		return Void.class;
	}

}
