package benchmark.rpc.netty.serializer;

import java.util.IdentityHashMap;

public class Register {
	private final static Serializer<?>[] idToSerializer = new Serializer[16];
	private final static IdentityHashMap<Class<?>, Integer> classToId = new IdentityHashMap<>();
	private final static IdentityHashMap<Class<?>, Serializer<?>> classToSerializer = new IdentityHashMap<>();

	static {
		int index = 0;

		idToSerializer[index++] = new VoidSerializer();
		idToSerializer[index++] = new IntegerSerializer();
		idToSerializer[index++] = new LongSerializer();
		idToSerializer[index++] = new BooleanSerializer();
		idToSerializer[index++] = new StringSerializer();
		idToSerializer[index++] = new LocalDateSerializer();
		idToSerializer[index++] = new LocalTimeSerializer();
		idToSerializer[index++] = new LocalDateTimeSerializer();
		idToSerializer[index++] = new UserSerializer();
		idToSerializer[index++] = new UserPageSerializer();
		idToSerializer[index++] = new RequestSerializer();

		for (int i = 0; i < index; i++) {
			Serializer<?> serializer = idToSerializer[i];
			Class<?> clazz = serializer.typeClass();

			classToSerializer.put(clazz, serializer);
			classToId.put(clazz, i);
		}
	}

	public static Serializer<?> serializer(int id) {
		return idToSerializer[id];
	}

	public static Serializer<?> serializer(Class<?> clazz) {
		return classToSerializer.get(clazz);
	}

	public static int id(Class<?> clazz) {
		return classToId.get(clazz);
	}
}
