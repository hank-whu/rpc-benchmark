# 运行说明
1. 需要两台机器，一台作为客户端，一台作为服务端.
2. 客户端需要安装 git, jdk 9, scala 12, maven 3.
3. 服务端需要安装 jdk 9.
4. 客户端服务端均需要设置 hosts.
> 10.0.0.88 benchmark-client
> 10.0.0.99 benchmark-server

5. 服务端需要添加用户 benchmark, 需要配置成客户端免密登录, 也就是客户端可以通过如下方式访问服务端.
> ssh benchmark@benchmark-server "ls -lh"

6. 客户端执行如下命令, 结果输出到 benchmark/benchmark-result.
> git clone https://github.com/hank-whu/rpc-benchmark.git
> cd rpc-benchmark
> scala benchmark.scala

