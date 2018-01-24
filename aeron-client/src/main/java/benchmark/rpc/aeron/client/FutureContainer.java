package benchmark.rpc.aeron.client;

import java.util.concurrent.ConcurrentHashMap;

import benchmark.rpc.protocol.Response;

/**
 * only for benchmark.rpc.netty.client, do not use it, it is not safe
 * 
 * @author Hank
 *
 */
final class FutureContainer {
	public static final int CLEAN_PERIOD = 10;

	private final ConcurrentHashMap<Long, CallerBlockingFuture<Response>> futureMap = new ConcurrentHashMap<>();

	public void addFuture(long requestId, CallerBlockingFuture<Response> future) {
		futureMap.put(requestId, future);
	}

	public void remove(long requestId) {
		futureMap.remove(requestId);
	}

	public void notifyResponse(Response response) {
		CallerBlockingFuture<Response> future = futureMap.remove(response.getRequestId());

		if (future == null) {
			return;
		}

		future.setValue(response);
	}

}
