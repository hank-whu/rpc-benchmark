package benchmark.rpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jupiter.common.util.SystemPropertyUtil;
import org.jupiter.rpc.DefaultClient;
import org.jupiter.rpc.JClient;
import org.jupiter.rpc.consumer.ProxyFactory;
import org.jupiter.rpc.load.balance.LoadBalancerType;
import org.jupiter.serialization.SerializerType;
import org.jupiter.spring.support.JupiterSpringClient;
import org.jupiter.transport.*;
import org.jupiter.transport.JConfig;
import org.jupiter.transport.JOption;
import org.jupiter.transport.UnresolvedAddress;
import org.jupiter.transport.netty.JNettyTcpConnector;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.JupiterUserService;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@State(Scope.Benchmark)
public class Client {
	public static final int CONCURRENCY = 32;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final UserService _serviceUserService = new UserServiceServerImpl();

	private final JupiterUserService userService;
	private final JClient client;

	public Client() {
		SystemPropertyUtil.setProperty("jupiter.tracing.needed", "false");
		client = new DefaultClient().withConnector(new JNettyTcpConnector(true));
		JConfig config = client.connector().config();
		config.setOption(JOption.WRITE_BUFFER_HIGH_WATER_MARK, 2048 * 1024);
		config.setOption(JOption.WRITE_BUFFER_LOW_WATER_MARK, 1024 * 1024);
		config.setOption(JOption.SO_RCVBUF, 256 * 1024);
		config.setOption(JOption.SO_SNDBUF, 256 * 1024);

		UnresolvedAddress[] addresses = new UnresolvedAddress[4];
		JConnector<JConnection> connector = client.connector();
		for (int i = 0; i < addresses.length; i++) {
			addresses[i] = new UnresolvedAddress("benchmark-server", 18090);
			JConnection connection = connector.connect(addresses[i]);
			connector.connectionManager().manage(connection);
		}

		userService = ProxyFactory.factory(JupiterUserService.class).client(client).addProviderAddress(addresses)
				.newProxyInstance();
	}

	@TearDown
	public void close() throws IOException {
		client.shutdownGracefully();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public boolean existUser() throws Exception {
		String email = String.valueOf(counter.getAndIncrement());
		return userService.existUser(email);
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public boolean createUser() throws Exception {
		int id = counter.getAndIncrement();
		User user = _serviceUserService.getUser(id);
		return userService.createUser(user);
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public User getUser() throws Exception {
		int id = counter.getAndIncrement();
		return userService.getUser(id);
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Page<User> listUser() throws Exception {
		int pageNo = counter.getAndIncrement();
		return userService.listUser(pageNo);
	}

	public static void main(String[] args) throws Exception {
		Client client = new Client();
		System.out.println(client.getUser());
		client.close();

		Options opt = new OptionsBuilder()//
				.include(Client.class.getSimpleName())//
				.warmupIterations(10)//
				.measurementIterations(3)//
				.threads(CONCURRENCY)//
				.forks(1)//
				.build();

		new Runner(opt).run();
	}

}
