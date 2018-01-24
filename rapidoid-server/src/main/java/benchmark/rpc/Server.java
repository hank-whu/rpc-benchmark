package benchmark.rpc;

import org.rapidoid.config.Conf;
import org.rapidoid.setup.App;
import org.rapidoid.setup.My;
import org.rapidoid.setup.On;

import benchmark.rpc.util.JsonUtils;

public class Server {

	public static final String host = "benchmark-server";
	public static final int port = 8080;

	public static void main(String[] args) {

		App.bootstrap(args);

		My.custom().objectMapper(JsonUtils.objectMapper);

		Conf.HTTP.set("maxPipeline", 256);
		Conf.HTTP.set("timeout", 0);
		Conf.HTTP.sub("mandatoryHeaders").set("connection", false);

		On.port(port);
	}

}
