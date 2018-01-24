package benchmark.rpc.aeron.client;

import benchmark.rpc.protocol.Response;

public interface ResponseCallback {
	public void onResponse(Response response);
}
