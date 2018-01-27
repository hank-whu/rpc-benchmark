/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmark.rpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.jupiter.monitor.MonitorServer;
import org.jupiter.registry.RegistryServer;

/**
 * 1.启动 SpringRegistryServer 2.再启动 SpringServer 3.最后启动 SpringClient
 *
 * jupiter org.jupiter.example.spring
 *
 * @author jiachun.fjc
 */
public class SpringJupiterRegistryServer {

	public static void main(String[] args) {
		SocketAddress registryServerAddress = new InetSocketAddress("benchmark-server", 20001);
		RegistryServer registryServer = RegistryServer.Default.createRegistryServer(registryServerAddress, 1); // 注册中心
		MonitorServer monitor = new MonitorServer(19998); // 监控服务
		try {
			monitor.setRegistryMonitor(registryServer);
			monitor.start();
			registryServer.startRegistryServer();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
