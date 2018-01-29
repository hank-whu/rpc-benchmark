# RPC Benchmark
 - 仅限于Java.
 - 客户端使用JMH进行压测, 32 线程, 10 次预热, 3 次运行.
 - 每次运行前都会执行 ***killall java***, 但没有在每轮测试时重启操作系统.
 - 所有类库版本在发布时都是最新的, 除非存在bug.
 - 所有框架都尽量参考该项目自带的Benchmark实现.
 - 将会一直持续, 不定期发布测试结果.

# 运行说明
1. 需要两台机器，一台作为客户端，一台作为服务端.
2. 系统要求为 linux x64, 至少 4GB ram.
3. 客户端需要安装 git, jdk 9, scala 12, maven 3.
4. 服务端需要安装 jdk 9.
5. 客户端服务端均需要设置 hosts.
> 10.0.0.88 benchmark-client<br>
> 10.0.0.99 benchmark-server

6. 服务端需要添加用户 benchmark, 需要配置成客户端免密登录, 也就是客户端可以通过如下方式访问服务端.
> ssh benchmark@benchmark-server "ls -lh"

7. 客户端执行如下命令, 结果输出到 benchmark/benchmark-result.
> git clone https://github.com/hank-whu/rpc-benchmark.git<br>
> cd rpc-benchmark<br>
> scala benchmark.scala

# 免责声明
 - 能力所限错误在所难免, 本测试用例及测试结果仅供参考.
 - 如果你认为xx框架的代码或配置存在问题，那么欢迎发起Pull Request.
 - 利益相关: 本测试用例作者同时为 [turbo](https://github.com/hank-whu/turbo-rpc), [undertow-async](https://github.com/hank-whu/undertow-async) 的作者.