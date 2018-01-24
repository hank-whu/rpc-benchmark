package benchmark.rpc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Server {

	public static void main(String[] args) throws InterruptedException {
		new Thread(() -> SpringJupiterRegistryServer.main(args)).start();

		Thread.sleep(1000);

		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-provider.xml");) {
			context.start();
			System.out.println("Jupiter started");
			Thread.sleep(Integer.MAX_VALUE);
		}
	}

}
