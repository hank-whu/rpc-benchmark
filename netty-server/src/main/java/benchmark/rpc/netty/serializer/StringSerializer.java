package benchmark.rpc.netty.serializer;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

public class StringSerializer implements Serializer<String> {

	@Override
	public void write(ByteBuf byteBuf, String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		byteBuf.writeInt(bytes.length);
		byteBuf.writeBytes(bytes);
	}

	@Override
	public String read(ByteBuf byteBuf) {
		int length = byteBuf.readInt();
		byte[] bytes = new byte[length];
		byteBuf.readBytes(bytes);

		return new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public Class<String> typeClass() {
		return String.class;
	}

}
