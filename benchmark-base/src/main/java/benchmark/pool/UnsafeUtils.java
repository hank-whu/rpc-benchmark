package benchmark.pool;

import sun.misc.Unsafe;

public class UnsafeUtils {
	final static private Unsafe _unsafe;

	static {
		Unsafe tmpUnsafe = null;

		try {
			java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			tmpUnsafe = (sun.misc.Unsafe) field.get(null);
		} catch (java.lang.Exception e) {
			throw new Error(e);
		}

		_unsafe = tmpUnsafe;
	}

	public static final Unsafe unsafe() {
		return _unsafe;
	}
}
