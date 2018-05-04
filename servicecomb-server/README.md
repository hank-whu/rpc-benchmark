### Package Producer

```bash
mvn package
```

### Deploy Producer

Copy `lib` folder(Dependencies) and `servicecomb-server-round-x.jar` to your server.

*Notice: If you need to modify configurations in "microservice.yaml" like service center address but don't want repackage the executable jar, you can directly place a new "microservice.yaml" file under the same folder, then configurations will be overridden.

### Run Producer
#### JDK8:

```bash
java -jar servicecomb-server-round-3.jar
```

#### JDK9:

```bash
java --add-modules=java.xml.ws -jar servicecomb-server-round-3.jar
```