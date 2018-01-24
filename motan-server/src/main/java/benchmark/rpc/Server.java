package benchmark.rpc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Server {

	public static void main(String[] args) throws InterruptedException {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("motan_server.xml");) {
			context.start();
			Thread.sleep(Integer.MAX_VALUE);
		}
	}

}
