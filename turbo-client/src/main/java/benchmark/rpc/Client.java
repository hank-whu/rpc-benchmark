package benchmark.rpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import benchmark.rpc.service.TurboUserService;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import rpc.turbo.client.TurboClient;

@State(Scope.Benchmark)
public class Client {
	public static final int CONCURRENCY = 32;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final UserService _serviceUserService = new UserServiceServerImpl();

	private final TurboClient client;
	private final TurboUserService userService;

	public Client() {
		client = new TurboClient("turbo-client.conf");

		try {
			client.register(TurboUserService.class);
			userService = client.getService(TurboUserService.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TearDown
	public void close() throws IOException {
		client.close();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean existUser() throws Exception {
		String email = String.valueOf(counter.getAndIncrement());
		return userService.existUser(email).join();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean createUser() throws Exception {
		int id = counter.getAndIncrement();
		User user = _serviceUserService.getUser(id);
		return userService.createUser(user).join();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public User getUser() throws Exception {
		int id = counter.getAndIncrement();
		return userService.getUser(id).join();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Page<User> listUser() throws Exception {
		int pageNo = counter.getAndIncrement();
		return userService.listUser(pageNo).join();
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder()//
				.include(Client.class.getSimpleName())//
				.warmupIterations(5)//
				.measurementIterations(5)//
				.threads(CONCURRENCY)//
				.forks(1)//
				.build();

		new Runner(opt).run();
	}

}
