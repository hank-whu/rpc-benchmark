package benchmark.rpc;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceJsonHttpClientImpl;

@State(Scope.Benchmark)
public class Client extends AbstractClient {
	public static final int CONCURRENCY = 32;

	private final UserService userService = new UserServiceJsonHttpClientImpl(CONCURRENCY);

	@Override
	protected UserService getUserService() {
		return userService;
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

	public static void main(String[] args) throws RunnerException {
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
	}

}
