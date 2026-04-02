# Eclipse DataGrid Cluster Hibernate Cache Demo

Demo application featuring a bookstore demo REST-application using the EclipseStore
backed [Eclipse DataGrid Clustered Cache](https://github.com/eclipse-datagrid/datagrid). This app uses Kafka to
distribute Hibernate update timestamps to all other running cache nodes.

## Starting the application

This application requires a running Kafka server. For more information about deploying
see [the docs.](https://docs.microstream.one/enterprise/manual/1/hibernateCache/deployment.html)

## Endpoints
To get a documented list of every endpoint the [/swagger-ui](http://localhost:8080/swagger-ui) endpoint can be called at runtime. This uses an OpenAPI definition file which is generated when the project is built. To view this file without starting the application, execute the following command:

```shell
mvn clean package
```

afterward the file should be located at `target/classes/META-INF/swagger/cluster-storage-demo-1.0.yml`

### Testing
The _testing_ directory contains a Postman collection which can be imported in Postman for easy testing. It also contains bash and batch scripts for calling the endpoints via the _curl_ executable. To use these simply check out the documentation comments at the start of each script file.

Example usage for the insert genre endpoint:

```shell
testing/genre/insert.sh thriller
```
