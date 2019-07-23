package benchmark.rpc;

import benchmark.service.UserServiceServerImpl;
import com.baidu.brpc.protocol.Options;
import com.baidu.brpc.server.RpcServer;
import com.baidu.brpc.server.RpcServerOptions;

public class Server {

    public static void main(String[] args) throws InterruptedException {
        int port = 8002;

        RpcServerOptions options = new RpcServerOptions();
        options.setReceiveBufferSize(64 * 1024 * 1024);
        options.setSendBufferSize(64 * 1024 * 1024);
        options.setKeepAliveTime(20);
        options.setProtocolType(Options.ProtocolType.PROTOCOL_HTTP_JSON_VALUE);

        final RpcServer rpcServer = new RpcServer(port, options);
        rpcServer.registerService(new UserServiceServerImpl());
        rpcServer.start();

        System.out.println("START");
        Thread.sleep(Long.MAX_VALUE);
    }

}
