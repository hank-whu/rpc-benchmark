import java.io.{ BufferedReader, File, FileOutputStream, InputStreamReader }
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object benchmark {
	val resultFolder = new File("benchmark-result")

	def main(args : Array[String]) : Unit = {
		installBenchmarkBase()

		val allTasks = getAllTasks()
		println("找到以下benchmark项目:")
		println(allTasks.mkString(", "))

		getAllTasks().foreach(benchmark(_))
	}

	def installBenchmarkBase() {
		exec("benchmark-base", "mvn clean install")
	}

	def getAllTasks() : Array[String] = {
		val folder = new File(".")

		folder
			.list
			.filter(_.endsWith("-client"))
			.map(name => name.substring(0, name.length() - "-client".length()))
			.sorted
	}

	def benchmark(taskName : String) {
		val serverPackage = packageAndGet(new File(taskName + "-server"))
		val clientPackage = packageAndGet(new File(taskName + "-client"))

		startServer(serverPackage)

		//等服务器启动起来在启动客户端
		TimeUnit.MILLISECONDS.sleep(15)

		startClient(clientPackage)

		stopServer(serverPackage)
	}

	def packageAndGet(project : File) : File = {
		exec(project, "mvn clean package")

		new File(project, "target")
			.listFiles()
			.find(_.getName.endsWith("-jar-with-dependencies.jar"))
			.get
	}

	def startServer(serverPackage : File) {
		val name = serverPackage.getName
		println(s"start $name")

		val resultPath = name.substring(0, name.length() - "-jar-with-dependencies.jar".length()) + ".log"

		//copy到benchmark-server
		exec(serverPackage.getParentFile, s"scp ${name} benchmark@benchmark-server:~")

		//杀掉benchmark-server上的老进程
		exec(Array("ssh", "benchmark@benchmark-server", "killall java"))

		//benchmark-server上启动服务器
		val remoteCommand = s"nohup java -server -Xmx1g -Xms1g -XX:MaxDirectMemorySize=1g -XX:+UseG1GC -jar ${name} >> ${resultPath} &"
		exec(Array("ssh", "benchmark@benchmark-server", remoteCommand))
	}

	def stopServer(serverPackage : File) {
		val name = serverPackage.getName
		println(s"stop $name")

		//benchmark-server上启动服务器
		exec("ssh benchmark@benchmark-server \"killall java\"")
	}

	def startClient(clientPackage : File) {
		val name = clientPackage.getName
		println(s"start $name")

		val resultFile = new File(resultFolder, name.substring(0, name.length() - "-jar-with-dependencies.jar".length()) + ".log")

		val command = s"java -server -Xmx1g -Xms1g -XX:MaxDirectMemorySize=1g -XX:+UseG1GC -jar ${name}"

		//启动客户端
		exec(clientPackage.getParentFile, command, resultFile)
	}

	def exec(path : String, command : String) {
		if (path != null) exec(new File(path), command)
		else exec(null.asInstanceOf[File], command)
	}

	def exec(command : String) {
		exec(null.asInstanceOf[File], command)
	}

	def exec(file : File, command : String, redirect : File = null) {
		val process = Runtime.getRuntime().exec(command, null, file)

		val inputStream = process.getInputStream()
		val inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
		val reader = new BufferedReader(inputStreamReader)

		if (redirect != null && !redirect.exists()) {
			redirect.getParentFile.mkdirs();
		}

		val output : FileOutputStream =
			if (redirect != null) new FileOutputStream(redirect) else null

		try {
			var line = ""
			while ({ line = reader.readLine(); line != null }) {
				println(line)

				if (output != null) {
					output.write(line.getBytes("UTF-8"))
					output.write("\r\n".getBytes("UTF-8"))
				}
			}
		} catch {
			case t : Throwable => t.printStackTrace()
		} finally {
			reader.close()
			inputStreamReader.close()
			inputStream.close()

			if (output != null) {
				output.flush()
				output.close()
			}

		}

		process.waitFor();
		process.destroy();
	}

	def exec(commands : Array[String]) {
		val process = Runtime.getRuntime().exec(commands)

		val inputStream = process.getInputStream()
		val inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
		val reader = new BufferedReader(inputStreamReader)

		try {
			var line = ""
			while ({ line = reader.readLine(); line != null }) {
				println(line)
			}
		} catch {
			case t : Throwable => t.printStackTrace()
		} finally {
			reader.close()
			inputStreamReader.close()
			inputStream.close()
		}

		process.waitFor();
		process.destroy();
	}

}