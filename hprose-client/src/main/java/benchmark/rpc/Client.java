package benchmark.rpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
import benchmark.service.UserService;
import hprose.client.HproseTcpClient;

@State(Scope.Benchmark)
public class Client extends AbstractClient {
	public static final int CONCURRENCY = 32;

	private final HproseTcpClient client = new HproseTcpClient("tcp://benchmark-server:8080");
	private final UserService userService = client.useService(UserService.class);

	public Client() {
		client.setFullDuplex(true);
		client.setNoDelay(true);
	}
	
	@Override
	protected UserService getUserService() {
		return userService;
	}

	@TearDown
	public void close() throws IOException {
		client.close();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Override
	public boolean existUser() throws Exception {
		return super.existUser();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Override
	public boolean createUser() throws Exception {
		return super.createUser();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Override
	public User getUser() throws Exception {
		return super.getUser();
	}

	@Benchmark
	@BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Override
	public Page<User> listUser() throws Exception {
		return super.listUser();
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder()//
				.include(Client.class.getSimpleName())//
				.warmupIterations(10)//
				.measurementIterations(3)//
				.threads(CONCURRENCY)//
				.forks(1)//
				.build();

		new Runner(opt).run();
//		HproseTcpClient client = new HproseTcpClient("tcp://127.0.0.1:8080");
//		UserService userService = client.useService(UserService.class);
//		System.out.println(userService.existUser("1"));
	}

}
