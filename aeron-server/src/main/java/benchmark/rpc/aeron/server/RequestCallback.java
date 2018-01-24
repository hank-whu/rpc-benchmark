package benchmark.rpc.aeron.server;

import benchmark.rpc.protocol.Request;

public interface RequestCallback {
	public void onRequest(Request request);
}
