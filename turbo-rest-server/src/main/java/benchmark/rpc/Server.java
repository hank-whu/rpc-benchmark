package benchmark.rpc;

import java.util.Map;

import benchmark.rpc.service.TurboUserService;
import benchmark.rpc.service.TurboUserServiceServerImpl;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import rpc.turbo.config.HostPort;
import rpc.turbo.server.TurboServer;

public class Server {

	public static void main(String[] args) throws Exception {
		ResourceLeakDetector.setLevel(Level.DISABLED);

		try (TurboServer server = new TurboServer("shop", "auth");) {
			Map<Class<?>, Object> services = Map.of(TurboUserService.class, new TurboUserServiceServerImpl());
			server.registerService(services);

			server.startRestServer(new HostPort("benchmark-server", 8080));
			server.waitUntilShutdown();
		}
	}

}
