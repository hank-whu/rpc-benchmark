package benchmark.rpc.grpc;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcUserServiceClient implements Closeable {

	public final ManagedChannel channel;
	public final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

	public GrpcUserServiceClient(String host, int port) {
		ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder//
				.forAddress(host, port)//
				.idleTimeout(5, TimeUnit.SECONDS)//
				.usePlaintext(true);

		channel = channelBuilder.build();
		userServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel);
	}

	@Override
	public void close() throws IOException {
		try {
			channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

}
