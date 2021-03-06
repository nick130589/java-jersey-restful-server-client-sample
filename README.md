
# java-jersey-restful-server-client-sample
[![Build Status](https://travis-ci.org/iwag/java-jersey-restful-server-client-sample.svg?branch=master)](https://travis-ci.org/iwag/java-jersey-restful-server-client-sample)
[![MIT License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](LICENSE)

RESTful sample with [Jersey](https://jersey.github.io/) Server/Client in Java8. This sample is simple ToDo Application in Command line interface.

# Features

* [Server side project](./projserver)
* [Introduce Web Test by DI](./projserver/src/test)
* [Client side code](./projclient)
* [Step-by-Step guide to build a API server/client](./README.md#make_simple_crud)


# Table of Contents

1. [How to run](#how_to_run)
2. [Make A Simple Server](#make_simple_crud)

<a name="how_to_run"></a>
# How to run

Server:

```bash
cd projserver
mvn test clean package
java -jar target/dependency/jetty-runner.jar target/*.war
```

Client:

```
cd projclient
mvn package
java -jar ...
```

<a name="make_simple_crud"></a>
# Make a simple CRUD server

First of all, we need to create a model class. This class is a resouce and subject to deal with API.

```Task.java
@XmlRootElement // important
public class Task {
    private String description;
    private Integer priority;
    private String untilDate;

    public Task(String description, Integer priority, String untilDate) {
        this.description = description;
        this.priority = priority;
        this.untilDate = untilDate;
    }

    public Task() { // important
    }

    // getter and setter methods are followed. this is also nessesary
```

Next, create a resouce class with GET method.

```TaskResource.java
@Path("task") // is supporsed to be http://BASE_URL/task
public class TaskResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Task get() {
        return new Task("sample", 0, "2017/08/10");
    }

}
```

main functions is like this. Set some setting for running as server.

```Main.java
public class Main {
    public static final Integer PORT = 8080;

    public static void main(String[] args) throws Exception {
        final Logger logger = LogManager.getLogger();
        final Server server = new Server(Integer.valueOf(PORT));
        final WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setParentLoaderPriority(true);

        final String webappDirLocation = "src/main/webapp/";
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);

        server.setHandler(root);

        logger.info("start " + server.getURI() + "...");
        server.start();
        server.join();
    }
}
```

Compile and run (see above)

```bash
java -jar target/dependency/jetty-runner.jar target/*.war 2>&1                                                     2017-08-14 02:04:25.580:INFO::main: Logging initialized @262ms
2017-08-14 02:04:25.589:INFO:oejr.Runner:main: Runner
2017-08-14 02:04:25.713:INFO:oejs.Server:main: jetty-9.3.3.v20150827
2017-08-14 02:04:29.446:INFO:oejsh.ContextHandler:main: Started o.e.j.w.WebAppContext@e6ea0c6{/,file:///Users/iwag/Devel/src/github.com/iwag/java-jersey-restful-server-client-sample/projserver/target/projserver/,AVAILABLE}{file:///Users/iwag/Devel/src/github.com/iwag/java-jersey-restful-server-client-sample/projserver/target/projserver.war}
2017-08-14 02:04:29.485:INFO:oejs.ServerConnector:main: Started ServerConnector@7daa0fbd{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
2017-08-14 02:04:29.485:INFO:oejs.Server:main: Started @4169ms
```

Let's get http request to it via cURL.

```
$ curl -i -H "Content-Type: application/json" 'localhost:8080/task'
HTTP/1.1 200 OK
Date: Mon, 14 Aug 2017 09:05:31 GMT
Content-Type: application/json
Content-Length: 62
Server: Jetty(9.3.3.v20150827)

{"description":"sample","priority":0,"untilDate":"2017/08/10"}%
```

Well done. Now we've done with GET(READ) among CRUD.
CUD is implemented like following code.

```TaskResource.java
    @DELETE
    public Response delete() {
        // write deleting code
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(Task t) {
        System.out.println(t);
        // write deleting code
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public Response create(Task t) {
        System.out.println(t);
        // write creating code
        return Response.status(Response.Status.ACCEPTED).build();
    }

```

To allow server to accept a request's body as JSON, just use Consumes. When response no body, you might want to use ResponseBuilder.

Let's run.

```
$ curl -XPOST -i -H "Content-Type: application/json" 'localhost:8080/task' -d'{"description":"", "priority":1, "untilDate":""}'
HTTP/1.1 202 Accepted
Date: Mon, 14 Aug 2017 10:42:34 GMT
Content-Length: 0
Server: Jetty(9.3.3.v20150827)

$ curl -XPUT -i -H "Content-Type: application/json" 'localhost:8080/task' -d'{"description":"", "priority":1, "until_date":""}'
HTTP/1.1 202 Accepted
Date: Mon, 14 Aug 2017 10:43:45 GMT
Content-Length: 0
Server: Jetty(9.3.3.v20150827)

$ curl -XDELETE -i -H "Content-Type: application/json" 'localhost:8080/task'
HTTP/1.1 202 Accepted
Date: Mon, 14 Aug 2017 10:43:55 GMT
Content-Length: 0
Server: Jetty(9.3.3.v20150827)
```

To deal with parameter in request path, use @Path and @PathParam annotations.

```java
    @GET
	@Path("{id}") // match a value of PathParam
    @Produces({MediaType.APPLICATION_JSON})
    public Task[] get(@PathParam("id") String id) {
        return new Task[]{new Task("sample", 0, "2017/08/10")};
    }
```

Now we can get id by url like "localhost:8080/task/0" and 0 is set at argument id.

<a name="jaxb_tip"></a>
## JAXB tip

Sometimes field names in Java is not appropriate for JSON convention such as mixup between snake_case and camelCase.
At that time, use XmlElement annotation to change field name as JSON field.

```Task.java
    @XmlElement(name = "until_date")
    private String untilDate;
```

If it returns as JSArray, just take a []

```java
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Task[] gets() {
        return new Task[]{new Task("sample", 0, "2017/08/10")};
    }
```


# Test web server using by DI (Dependency Injection)

## Preps in server

When we want to test only a web server instead of Database and other library depended by middleware,
There's effective technique called Dependency Injection.
In this project, we use [hk2 (JSR-330) library](https://javaee.github.io/hk2/).
There's another popular library at DI in Java, [guice]() in google and usually popular Web Frameworks have a feature like that nowadays.

Firstly, we seperate Database access into Service interface and implement class.
Meanwhile we're using @Contract annotation in Service interface and @Service annotation in implementation class.
In resource, we get data through this service class and put a @Inject annotation with service field.


```TaskService.java
@Contract
interface TaskService {
   Task get(String id);
}
```

```TaskServiceImpl.java
@Service
interface TaskServiceImpl {
   Task get(String id) {
      return new Task();
   }
}
```

```TaskResource.java
@Path("task")
public class TaskResource {
   @Inject
   TaskService taskService;
   ...
}
```

Furthermore Resource configuration class is neccesarry.

```TaskConfig.java
@ApplicationPath("rest")
public class TaskConfig extends ResourceConfig {

    @Inject
    public TaskConfig(ServiceLocator locator) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.bind(BuilderHelper.link(TaskRepositoryInMem.class).to(TaskRepository.class).build());

        config.commit();

        packages(true, getClass().getPackage().getName());
    }
}
```

Finally, modify web.xml(src/webapp/WEB-INF) to ensure this configuration.

```
<init-param>
  <param-name>javax.ws.rs.Application</param-name>
  <param-value>io.github.iwag.jerseystarter.main.TaskConfig</param-value>
</init-param>
```

## Server side test with DI

Now We can create test by using DI service and contract.
We can inject Service implementation in test like below.

```
public class TestTask extends JerseyTest {

    @Service
    static class TaskServiceMock implements TaskService {
        static UserEntity userEntity = null;

        public TaskServiceMock() {
        }

        // implement these for test
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(TaskResource.class).register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(TaskServiceMock.class).to(TaskService.class);
            }
        });
    }

    @Test
    public testGetTask0() {
    ...
    }

```

<a name="simple_client"></a>
# Simple Client for CRUD

We'd like to follow the [Repository Pattern](https://martinfowler.com/eaaCatalog/repository.html). It gives many benefits so easily to create test case and hide some environment specicic details. Make all CRUD methods in Repository interface.

```java
interface TaskRepository {
    Task[] gets();
    Task get(String id);
    boolean create(Task t);
    boolean update(String id, Task t);
    boolean delete(String id);
}
```

Implement ApiTaskRepository class with the interface. Here we need to implement using jersey client and call API server.

```java
public class ApiTaskRepository implements TaskRepository {
    @Override
    public Task[] gets() {
        // Write API requesting code
        return new Task[0];
    }

    @Override
    public Task get(String id) {
        // Write API requesting code
        return null;
    }

    @Override
    public boolean create(Task t) {
        // Write API requesting code
        return true;
    }

    @Override
    public boolean update(String id, Task t) {
        // Write API requesting code
        return true;
    }

    @Override
    public boolean delete(String id) {
        // Write API requesting code
        return true;
    }
}
```

Lets create test case at first. In addition, this test utilizes technique so called mocking.

```java
public class TaskTest extends Mockito {
  static Client mockClient(Response.Status status, Task t) {
    // see  https://github.com/iwag/java-jersey-restful-server-client-sample/blob/55e07a8de20f9f37624cfa8699b5ad245e33424b/projclient/src/test/java/io/github/iwag/jerseystarter/TaskTest.java#L18
    ...
  }

  @Test
  public void testGetTask0_OK() {
      Task expected = new Task();
      Client clientMock = mockClient(Response.Status.OK, expected);
      Task t = new ApiTaskRepository(clientMock).get("0");
      Assert.assertEquals(t, expected);
  }
```

If you run this test, you can see a failure.

Second of all, set some constant parameters in constructor.
Client is a class to request an API server which can use one more times so it should be a field of class.

```java
public class ApiTaskRepository implements TaskRepository {
    private final String baseUrl = "http://localhost:8080";
    private final Client client;

    public ApiTaskRepository() {
        // create client instance
        client = ClientBuilder.newClient();
    }
```

Lets create GET api. Write following code in ApiTaskRepository.java.

```java
@Override
public Task get(String id) {
    // make web target by url
    WebTarget webTarget = client.target(baseUrl).path("/task/" + id);

    Response res = null;
    // request by GET method without body
    res = webTarget.request(MediaType.TEXT_PLAIN).accept("application/json").get();
    // check response. For now we're checking only status code.
    if (res.getStatus() != Response.Status.OK.getStatusCode()) {
        return null;
    }
    // translate response body into Task class
    Task task = res.readEntity(Task.class);

    return task;
}

```

Now, you can see green light when executing a test.

For API request, there are two important things to make sure, path and response.
Path is ensured in `target("http://localhost:8080").path("/task/" + id)` and we set Task.class in `.get(Task.class)` to map Task class then we receive a response as Task class.

How about POST method?

```java
@Test
public void testCreate_OK() {
    Client clientMock = mockClient(Response.Status.ACCEPTED, null);
    boolean success = new ApiTaskRepository(clientMock).create(new Task(null, "test", 0, "2017/08/15"));
    Assert.assertTrue(success);
}
```

```java
@Override
public boolean create(Task t) {
    WebTarget webTarget = client.target(baseUrl).path("/task");

    Response res = null;
    Entity<?> entity = Entity.entity(t, MediaType.APPLICATION_JSON); // convert class to accept by jersey
    res = webTarget.request(MediaType.APPLICATION_JSON).accept("application/json").post(entity);
    // check if getting correct status code (ACCEPTED)
    if (res.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
        return false;
    }
    return true;
}
```

Finally, introduce Main.java in client side.

```java

public class Main {

    public static void main(String[] args) throws java.lang.Exception {
        ApiTaskRepository taskRepository = new ApiTaskRepository();
        System.out.println("GET /task/0 " + taskRepository.get("0"));
        System.out.println("PUT /task " + taskRepository.create(new Task(null, "aaa", 1, "2017/08/14")));
        return ;
    }
}
```


Run server program(see above instruction).


Along with that, let's run client program by launching IntelliJ `Run as ...` at Main.java.
It will show following output.

```
GET /task/0 Task{description='sample', priority=0, untilDate='2017/08/10'}
PUT /task true

Process finished with exit code 0
```
