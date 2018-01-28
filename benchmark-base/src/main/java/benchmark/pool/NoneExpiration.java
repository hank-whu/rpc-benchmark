package benchmark.pool;

import stormpot.Expiration;
import stormpot.SlotInfo;

final class NoneExpiration<T> implements Expiration<PoolableObject<T>> {
	@Override
	public boolean hasExpired(SlotInfo<? extends PoolableObject<T>> info) {
		return false;
	}
}