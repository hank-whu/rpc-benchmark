# RPC Benchmark
几乎所有的 RPC 框架都宣称自己是“高性能”的, 那么实际结果到底如何呢, 让我们来做一个性能测试吧.

## 测试结果
- Round-5 2019-02-11 https://www.jianshu.com/p/cdd94d0853c3
- Round-4 2018-08-05 https://www.jianshu.com/p/72b98dc67d9d
- Round-3 2018-05-12 https://www.jianshu.com/p/caf51f5cfbaa
- Round-2 2018-03-25 https://www.jianshu.com/p/f0f494cfce94
- Round-1 2018-01-28 https://www.jianshu.com/p/18c95649b1a4

## 测试说明
 - 仅限于Java.
 - 客户端使用JMH进行压测, 32 线程, 3 轮预热 3 轮测试 每轮 10s
 - 每次运行前都会执行 ***killall java***, 但没有在每轮测试时重启操作系统
 - 所有类库版本在发布时都是最新的, 除非存在bug
 - 所有框架都尽量参考该项目自带的Benchmark实现
 - 将会一直持续, 不定期发布测试结果
 - 更多说明请移步: [怎样对 RPC 进行有效的性能测试](https://www.jianshu.com/p/cbcdf05eaa5c)

## 测试用例
 1. boolean existUser(String email), 判断某个 email 是否存在
 2. boolean createUser(User user), 添加一个 User
 3. User getUser(long id), 根据 id 获取一个用户
 4. Page<User> listUser(int pageNo), 获取用户列表

## 运行说明
1. 需要两台机器，一台作为客户端，一台作为服务端
2. 系统要求为 linux x64, 至少 4GB ram
3. 客户端需要安装 jdk 11, maven 3
4. 服务端需要安装 jdk 11
5. 客户端服务端均需要设置 hosts
> 10.0.0.88 benchmark-client<br>
> 10.0.0.99 benchmark-server

6. 服务端需要添加用户 benchmark, 需要配置成客户端免密登录, 也就是客户端可以通过如下方式访问服务端
> ssh benchmark@benchmark-server "ls -lh"

7. 客户端执行如下命令, 结果输出到 benchmark/benchmark-result
> git clone https://github.com/hank-whu/rpc-benchmark.git<br>
> cd rpc-benchmark<br>
> java benchmark.java

## 开发者必读
1. cd benchmark-base && mvn install 
2. 配置好 hosts: benchmark-client benchmark-server
3. 修改或者实现 xxx-server xxx-client
4. 启动 Server, 然后启动 Client, 确保能不出错跑完所有测试项目
5. 提交 Pull Request

## 免责声明
 - 能力所限错误在所难免, 本测试用例及测试结果仅供参考.
 - 如果你认为xx框架的代码或配置存在问题，那么欢迎发起Pull Request.
 - 利益相关: 本测试用例作者同时为 [turbo](https://github.com/hank-whu/turbo-rpc), [undertow-async](https://github.com/hank-whu/undertow-async) 的作者.

## 关注微信公众号: rpcBenchmark
![rpcBenchmark](https://github.com/hank-whu/rpc-benchmark/raw/master/rpcBenchmark.jpg)