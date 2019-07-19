package benchmark.rpc;

import benchmark.rpc.grpc.UserServiceServer;
import benchmark.rpc.rsocket.server.UserServiceRsocketServerImpl;
import io.rsocket.RSocketFactory;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class Server {

    public static void main(String[] args){
        UserServiceServer userServiceServer = new UserServiceServer(new UserServiceRsocketServerImpl(), Optional.empty(), Optional.empty());
        CloseableChannel closeableChannel =
                RSocketFactory.receive()
                        .acceptor(
                                (setup, sendingSocket) -> Mono.just(new RequestHandlingRSocket(userServiceServer)))
                        .transport(TcpServerTransport.create(8080))
                        .start()
                        .block();

        // Block so we don't exit
        closeableChannel.onClose().block();
    }
}
