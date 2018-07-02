package benchmark.rpc.netty.serializer;

import java.io.IOException;

import benchmark.rpc.protocol.Request;
import benchmark.rpc.protocol.Response;
import io.netty.buffer.ByteBuf;

public class FastestSerializer {

	private static final RequestSerializer requestSerializer = new RequestSerializer();
	private static final ResponseSerializer responseSerializer = new ResponseSerializer();

	public static void writeRequest(ByteBuf byteBuf, Request request) throws IOException {
		int beginWriterIndex = byteBuf.writerIndex();
		byteBuf.writeInt(0);

		requestSerializer.write(byteBuf, request);

		int finishWriterIndex = byteBuf.writerIndex();
		int length = finishWriterIndex - beginWriterIndex - 4;

		byteBuf.setInt(beginWriterIndex, length);
	}

	public static Request readRequest(ByteBuf byteBuf) throws IOException {
		return requestSerializer.read(byteBuf);
	}

	public static void writeResponse(ByteBuf byteBuf, Response response) throws IOException {
		int beginWriterIndex = byteBuf.writerIndex();
		byteBuf.writeInt(0);

		responseSerializer.write(byteBuf, response);

		int finishWriterIndex = byteBuf.writerIndex();
		int length = finishWriterIndex - beginWriterIndex - 4;

		byteBuf.setInt(beginWriterIndex, length);
	}

	public static Response readResponse(ByteBuf byteBuf) throws IOException {
		return responseSerializer.read(byteBuf);
	}

}
