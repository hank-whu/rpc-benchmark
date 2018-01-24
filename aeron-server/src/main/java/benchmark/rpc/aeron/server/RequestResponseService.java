package benchmark.rpc.aeron.server;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import benchmark.rpc.protocol.Request;
import benchmark.rpc.protocol.Response;
import benchmark.rpc.route.RouteService;

public class RequestResponseService implements RequestCallback {

	private final Publisher publisher;
	private final RouteService routeService = new RouteService();

	public RequestResponseService(Publisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void onRequest(Request request) {
		final long requestId = request.getRequestId();
		final int serviceId = request.getServiceId();
		final Object[] params = request.getParams();

		Object result = routeService.invoke(serviceId, params);

		Response response = new Response();
		response.setRequestId(requestId);
		response.setStatusCode((byte) 1);
		response.setResult(result);

		try {
			publisher.publish(response, 15, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
		}
	}

}
