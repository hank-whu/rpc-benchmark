package benchmark.rpc.netty.serializer;

import benchmark.rpc.protocol.Response;
import io.netty.buffer.ByteBuf;

public class ResponseSerializer implements Serializer<Response> {

	private final ObjectSerializer objectSerializer = new ObjectSerializer();

	@Override
	public void write(ByteBuf byteBuf, Response response) {
		byteBuf.writeLong(response.getRequestId());
		byteBuf.writeByte(response.getStatusCode());
		objectSerializer.write(byteBuf, response.getResult());
	}

	@Override
	public Response read(ByteBuf byteBuf) {
		long requestId = byteBuf.readLong();
		byte statusCode = byteBuf.readByte();
		Object result = objectSerializer.read(byteBuf);

		Response response = new Response();
		response.setRequestId(requestId);
		response.setStatusCode(statusCode);
		response.setResult(result);

		return response;
	}

	@Override
	public Class<Response> typeClass() {
		return Response.class;
	}

}
