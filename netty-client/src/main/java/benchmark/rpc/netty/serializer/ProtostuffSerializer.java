package benchmark.rpc.netty.serializer;

import java.io.IOException;

import benchmark.rpc.protocol.Request;
import benchmark.rpc.protocol.Response;
import io.netty.buffer.ByteBuf;
import io.protostuff.ByteBufInput;
import io.protostuff.ByteBufOutput;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffSerializer {

	private static final Schema<Request> requestSchema = RuntimeSchema.getSchema(Request.class);
	private static final Schema<Response> responseSchema = RuntimeSchema.getSchema(Response.class);

	public static void writeRequest(ByteBuf byteBuf, Request request) throws IOException {
		int beginWriterIndex = byteBuf.writerIndex();
		byteBuf.writerIndex(beginWriterIndex + 4);

		ByteBufOutput output = new ByteBufOutput(byteBuf);
		requestSchema.writeTo(output, request);

		int finishWriterIndex = byteBuf.writerIndex();
		int length = finishWriterIndex - beginWriterIndex - 4;

		byteBuf.setInt(beginWriterIndex, length);
	}

	public static Request readRequest(ByteBuf byteBuf) throws IOException {
		ByteBufInput input = new ByteBufInput(byteBuf, true);

		Request request = new Request();
		requestSchema.mergeFrom(input, request);

		return request;
	}

	public static void writeResponse(ByteBuf byteBuf, Response response) throws IOException {
		int beginWriterIndex = byteBuf.writerIndex();
		byteBuf.writerIndex(beginWriterIndex + 4);

		ByteBufOutput output = new ByteBufOutput(byteBuf);
		responseSchema.writeTo(output, response);

		int finishWriterIndex = byteBuf.writerIndex();
		int length = finishWriterIndex - beginWriterIndex - 4;

		byteBuf.setInt(beginWriterIndex, length);
	}

	public static Response readResponse(ByteBuf byteBuf) throws IOException {
		ByteBufInput input = new ByteBufInput(byteBuf, true);

		Response response = new Response();
		responseSchema.mergeFrom(input, response);

		return response;
	}

}
