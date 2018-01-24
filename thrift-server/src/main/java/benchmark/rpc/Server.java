package benchmark.rpc;

import java.net.InetSocketAddress;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

import benchmark.rpc.thrift.UserService;
import benchmark.rpc.thrift.UserService.Iface;
import benchmark.rpc.thrift.UserServiceThriftServerImpl;

public class Server {

	public static void main(String[] args) throws TTransportException {
		InetSocketAddress serverAddress = new InetSocketAddress("benchmark-server", 8080);

		TNonblockingServerTransport serverSocket = new TNonblockingServerSocket(serverAddress);
		TThreadedSelectorServer.Args serverParams = new TThreadedSelectorServer.Args(serverSocket);
		serverParams.protocolFactory(new TBinaryProtocol.Factory());
		serverParams.processor(new UserService.Processor<Iface>(new UserServiceThriftServerImpl()));
		TServer server = new TThreadedSelectorServer(serverParams);
		server.serve();
	}

}
