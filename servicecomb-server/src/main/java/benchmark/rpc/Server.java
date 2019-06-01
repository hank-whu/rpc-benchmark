package benchmark.rpc;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;

import io.netty.util.concurrent.GlobalEventExecutor;

public class Server {

	public static void main(String[] args) throws Exception {
		Object exec = GlobalEventExecutor.INSTANCE;
		System.out.println(exec);

		Log4jUtils.init();
		BeanUtils.init();
	}

}
