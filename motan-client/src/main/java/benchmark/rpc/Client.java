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
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.weibo.api.motan.closable.ShutDownHook;
import com.weibo.api.motan.util.StatsUtil;

import benchmark.bean.MotanUser;
import benchmark.bean.Page;
import benchmark.service.MotanUserService;
import benchmark.service.MotanUserServiceServerImpl;

@State(Scope.Benchmark)
public class Client {
	public static final int CONCURRENCY = 32;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final MotanUserService _serviceUserService = new MotanUserServiceServerImpl();

	private final ClassPathXmlApplicationContext context;
	private final MotanUserService userService;

	public Client() {
		context = new ClassPathXmlApplicationContext("motan_client.xml");
		context.start();
		userService = (MotanUserService) context.getBean("userService"); // 获取远程服务代理
	}

	@TearDown
	public void close() throws IOException {
		context.close();
		ShutDownHook.runHook(true);

		if (!StatsUtil.executorService.isShutdown()) {
			StatsUtil.executorService.shutdownNow();
		}

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
		MotanUser user = _serviceUserService.getUser(id);
		return userService.createUser(user);
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public MotanUser getUser() throws Exception {
		int id = counter.getAndIncrement();
		return userService.getUser(id);
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Page<MotanUser> listUser() throws Exception {
		int pageNo = counter.getAndIncrement();
		return userService.listUser(pageNo);
	}

	public static void main(String[] args) throws Exception {
		Client client = new Client();

		for (int i = 0; i < 60; i++) {
			try {
				System.out.println(client.getUser().getAddress());
				break;
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(1000);
			}
		}

		client.close();

		Options opt = new OptionsBuilder()//
				.include(Client.class.getSimpleName())//
				.warmupIterations(3)//
				.warmupTime(TimeValue.seconds(60))//
				.measurementIterations(3)//
				.measurementTime(TimeValue.seconds(60))//
				.threads(CONCURRENCY)//
				.forks(1)//
				.build();

		new Runner(opt).run();

		System.exit(1);

		// Client client = new Client();
		// System.out.println(client.getUser());
	}

}
