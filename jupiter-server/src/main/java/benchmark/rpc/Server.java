package benchmark.rpc;

import benchmark.service.JupiterUserServiceServerImpl;
import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.rpc.DefaultServer;
import org.jupiter.rpc.JServer;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JOption;
import org.jupiter.transport.netty.JNettyTcpAcceptor;

public class Server {

	public static void main(String[] args) throws InterruptedException {

		try {
			int processors = Runtime.getRuntime().availableProcessors();
			SystemPropertyUtil.setProperty("jupiter.executor.factory.provider.core.workers",
					String.valueOf(processors));
			SystemPropertyUtil.setProperty("jupiter.executor.factory.affinity.thread", "true");
			SystemPropertyUtil.setProperty("jupiter.tracing.needed", "false");

			JServer server = new DefaultServer().withAcceptor(new JNettyTcpAcceptor(18090, true));
			JConfig config = server.acceptor().configGroup().child();
			config.setOption(JOption.WRITE_BUFFER_HIGH_WATER_MARK, 2048 * 1024);
			config.setOption(JOption.WRITE_BUFFER_LOW_WATER_MARK, 1024 * 1024);
			config.setOption(JOption.SO_RCVBUF, 256 * 1024);
			config.setOption(JOption.SO_SNDBUF, 256 * 1024);

			server.serviceRegistry().provider(new JupiterUserServiceServerImpl()).register();

			server.start(false);
			System.out.println("Jupiter started");
			Thread.sleep(java.lang.Integer.MAX_VALUE);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
