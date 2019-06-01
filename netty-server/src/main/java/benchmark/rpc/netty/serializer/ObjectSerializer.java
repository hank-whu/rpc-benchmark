package benchmark.rpc.netty.serializer;

import io.netty.buffer.ByteBuf;

@SuppressWarnings("unchecked")
public class ObjectSerializer implements Serializer<Object> {

	@Override
	public void write(ByteBuf byteBuf, Object object) {
		if (object == null) {
			byteBuf.writeInt(0);
			return;
		}

		Class<?> clazz = object.getClass();

		int id = Register.id(clazz);
		Serializer<Object> serializer = (Serializer<Object>) Register.serializer(id);

		byteBuf.writeInt(id);
		serializer.write(byteBuf, object);
	}

	@Override
	public Object read(ByteBuf byteBuf) {
		int id = byteBuf.readInt();
		Serializer<Object> serializer = (Serializer<Object>) Register.serializer(id);
		return serializer.read(byteBuf);
	}

	@Override
	public Class<Object> typeClass() {
		return Object.class;
	}

}
