package benchmark.rpc;

import org.junit.Test;
import org.openjdk.jmh.runner.RunnerException;

public class ClientTest {

	@Test
	public void test() {
		try {
			Client.main(null);
		} catch (RunnerException e) {
			e.printStackTrace();
		}
	}

}
