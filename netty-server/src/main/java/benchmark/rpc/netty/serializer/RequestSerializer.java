package benchmark.rpc.netty.serializer;

import benchmark.rpc.protocol.Request;
import io.netty.buffer.ByteBuf;

public class RequestSerializer implements Serializer<Request> {

	private final ObjectSerializer objectSerializer = new ObjectSerializer();

	@Override
	public void write(ByteBuf byteBuf, Request request) {
		Object[] params = request.getParams();

		byteBuf.writeLong(request.getRequestId());
		byteBuf.writeInt(request.getServiceId());

		byteBuf.writeInt(params.length);
		for (int i = 0; i < params.length; i++) {
			objectSerializer.write(byteBuf, params[i]);
		}
	}

	@Override
	public Request read(ByteBuf byteBuf) {
		long requestId = byteBuf.readLong();
		int serviceId = byteBuf.readInt();

		int size = byteBuf.readInt();
		Object[] params = new Object[size];
		for (int i = 0; i < size; i++) {
			params[i] = objectSerializer.read(byteBuf);
		}

		Request request = new Request();
		request.setRequestId(requestId);
		request.setServiceId(serviceId);
		request.setParams(params);

		return request;
	}

	@Override
	public Class<Request> typeClass() {
		return Request.class;
	}

}
