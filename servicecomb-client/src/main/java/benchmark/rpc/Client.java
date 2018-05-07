package benchmark.rpc;

import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.stereotype.Component;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import io.netty.util.concurrent.GlobalEventExecutor;

@State(Scope.Benchmark)
@Component
public class Client extends AbstractClient {
	public static final int CONCURRENCY = 32;

	@RpcReference(microserviceName = "benchmark", schemaId = "benchmark")
	private static UserService userService;

	static {
		Object exec = GlobalEventExecutor.INSTANCE;
		System.out.println(exec);

		try {
			Log4jUtils.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		BeanUtils.init();
	}

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

	public static void main(String[] args) throws Exception {
		Client client = new Client();

		for (int i = 0; i < 60; i++) {
			try {
				System.out.println(client.existUser());
				System.out.println(client.createUser());
				System.out.println(client.getUser());
				System.out.println(client.listUser());
				break;
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(1000);
			}
		}

		Options opt = new OptionsBuilder()//
				.include(Client.class.getSimpleName())//
				.warmupIterations(3)//
				.warmupTime(TimeValue.seconds(3))//
				.measurementIterations(3)//
				.measurementTime(TimeValue.seconds(3))//
				.threads(CONCURRENCY)//
				.forks(0)//
				.build();

		new Runner(opt).run();

		System.exit(1);
	}

}
