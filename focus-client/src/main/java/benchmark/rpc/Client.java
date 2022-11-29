package benchmark.rpc;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import com.dinstone.focus.client.ClientOptions;
import com.dinstone.focus.client.FocusClient;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Benchmark)
public class Client {
	public static final int CONCURRENCY = 32;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final UserService _serviceUserService = new UserServiceServerImpl();

	private final FocusClient client;

	private final UserService userService;

	public Client() {
		ResourceLeakDetector.setLevel(Level.DISABLED);

		ClientOptions option = new ClientOptions().setEndpoint("focus.client").connect("benchmark-server", 3333);
		 client = new FocusClient(option);
		try {
			 userService = client.importing(UserService.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TearDown
	public void close() throws IOException {
		client.destroy();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean existUser() throws Exception {
		String email = String.valueOf(counter.getAndIncrement());
		return userService.existUser(email);
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean createUser() throws Exception {
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
		Options opt = new OptionsBuilder()//
				.include(Client.class.getSimpleName())//
				.warmupIterations(3)//
				.warmupTime(TimeValue.seconds(10))//
				.measurementIterations(3)//
				.measurementTime(TimeValue.seconds(10))//
				.threads(CONCURRENCY)//
				.forks(1)//
				.build();

		new Runner(opt).run();
	}

}
