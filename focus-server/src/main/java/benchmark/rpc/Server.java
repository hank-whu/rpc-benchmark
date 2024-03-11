package benchmark.rpc;

import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import com.dinstone.focus.server.FocusServer;
import com.dinstone.focus.server.ServerOptions;


public class Server {

    public static void main(String[] args) throws InterruptedException {

        ServerOptions serverOptions = new ServerOptions("focus.server").listen(3333);

        // exporting service
        try (FocusServer focusServer = new FocusServer(serverOptions)) {
            focusServer.exporting(UserService.class, new UserServiceServerImpl());

            focusServer.start();

            Thread.sleep(Integer.MAX_VALUE);
        }

    }

}
