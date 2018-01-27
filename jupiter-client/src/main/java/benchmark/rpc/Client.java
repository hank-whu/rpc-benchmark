package benchmark.rpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jupiter.spring.support.JupiterSpringClient;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

	private final ClassPathXmlApplicationContext context;
	private final JupiterUserService userService;

	public Client() {
		context = new ClassPathXmlApplicationContext("classpath:spring-consumer.xml");
		userService = context.getBean(JupiterUserService.class);
	}

	@TearDown
	public void close() throws IOException {
		context.getBean(JupiterSpringClient.class).getClient().shutdownGracefully();
		context.close();
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
		client.context.getBeansOfType(Object.class).forEach((key, value) -> {
			System.out.println(value.getClass());
		});
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
