package benchmark.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import rpc.turbo.boot.EnableTurboServer;

@SpringBootApplication(scanBasePackages = { "benchmark" })
@EnableTurboServer
public class Server {

	public static void main(String[] args) throws Exception {
		ResourceLeakDetector.setLevel(Level.DISABLED);
		SpringApplication.run(Server.class, args);
	}

}
