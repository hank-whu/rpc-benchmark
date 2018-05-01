package benchmark.rpc;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;

public class Server {

	public static void main(String[] args) throws Exception {
		Log4jUtils.init();
		BeanUtils.init();
	}

}
