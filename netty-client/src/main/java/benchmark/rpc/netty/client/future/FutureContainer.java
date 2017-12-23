package benchmark.rpc.netty.client.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import benchmark.rpc.protocol.Response;

/**
 * 
 * @author Hank
 *
 */
public final class FutureContainer {
	private final ConcurrentHashMap<Long, CompletableFuture<Response>> futureMap = new ConcurrentHashMap<>();

	public void addFuture(long requestId, CompletableFuture<Response> future) {
		futureMap.put(requestId, future);
	}

	public void remove(long requestId) {
		futureMap.remove(requestId);
	}

	public void notifyResponse(Response response) {
		CompletableFuture<Response> future = futureMap.remove(response.getRequestId());

		if (future == null) {
			return;
		}

		future.complete(response);
	}

}
