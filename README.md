# java-microservice

- http://localhost:8081/h2-console
- http://localhost:8081/swagger-ui/index.html

## Steps to run a Java microservice from a .jar file

1. Delete all files and folders from `target` folder
2. Open a terminal and `cd` to the root of the microservice
2. Run `mvn clean install`
3. There will be a .jar file in `target` folder, provided that pom.xml has `<packaging>jar</packaging>` tag. The .jar file has all the dependencies (e.g. Spring libraries, Tomcat server) except the runtime. 
4. Run `mvn spring-boot:run` or `java -jar target/<jar-file-name>.jar`

## Docker command cheat sheet

- docker build . -t <dockerhub-user-name>/<image-name>:<tag>
- docker images
- docker run -d -p <port-of-local-machine>:<port-used-by-the-image> <image-id> (e.g. `docker run -d -p 8080:8081 22b19` -> The app should be available at `http://localhost:8080/` in the local machine)
- docker start <container-id> (It runs an existing container, not an image)
- docker stop <container-id> (It stops an existing container, not an image)
- docker ps
- docker image push docker.io/<dockerhub-user-name>/<image-name>:<tag>


