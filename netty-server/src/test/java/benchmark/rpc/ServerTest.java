package benchmark.rpc;

import org.junit.Test;

public class ServerTest {

	@Test
	public void test() {
		try {
			Server.main(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
