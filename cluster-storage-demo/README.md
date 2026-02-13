# Eclipse DataGrid Cluster Storage Demo
Demo application featuring a bookstore demo REST-application using the EclipseStore backed [Eclipse DataGrid Micronaut Nodelibrary](https://github.com/eclipse-datagrid/datagrid).

## Starting the application
To start the application execute the following command:

```shell
mvn mn:run
```

### Running tests

To run the JUnit tests execute the following command:

```shell
mvn test
```

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
