# Open Liberty Masterclass

This document contains the hands-on lab modules for the Open Liberty Masterclass.  It is intended to be used in conjunction with taught materials, however, feel free to give it a try, even if you're not in a Masterclass.

## Table of Contents

- [Open Liberty Masterclass](#open-liberty-masterclass)
  - [Table of Contents](#table-of-contents)
  - [Before you begin](#before-you-begin)
    - [Install Pre-requisites](#install-pre-requisites)
    - [Prime Maven and Docker Caches](#prime-maven-and-docker-caches)
  - [The Application](#the-application)
  - [Module 1: Build](#module-1-build)
  - [Module 2: Feature-based Build](#module-2-feature-based-build)
  - [Module 3: Application APIs](#module-3-application-apis)
  - [Module 4: Server Configuration](#module-4-server-configuration)
  - [Module 5: Externalizing Configuration](#module-5-externalizing-configuration)
  - [Module 6: Integration Testing](#module-6-integration-testing)
  - [Module 7: Docker](#module-7-docker)
    - [Overriding Dev Server Configuration](#overriding-dev-server-configuration)
  - [Module 8: Support Licensing](#module-8-support-licensing)
  - [Conclusion](#conclusion)

## Before you begin

### Install Pre-requisites

* A Java 8 JDK (e.g. https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=openj9)
* Apache Maven (https://maven.apache.org/)
* A git client
* An editor with Java support (e.g. Eclipse, VS Code, IntelliJ)
* Docker 
  * **Windows:** Set up Docker for Windows as described at https://docs.docker.com/docker-for-windows/#kubernetes.
  *  **Mac:** Set up Docker for Mac as described at https://docs.docker.com/docker-for-mac/#kubernetes.
  *  After following one of the sets of instructions, ensure that Kubernetes (not Swarm) is selected as the orchestrator in Docker Preferences.
  *  **Linux:** You will use Minikube as a single-node Kubernetes cluster that runs locally in a virtual machine. For Minikube installation instructions see https://github.com/kubernetes/minikube. Make sure to read the "Requirements" section as different operating systems require different prerequisites to get Minikube running.

### Prime Maven and Docker Caches

If you will be taking the Masterclass at a location with limited network bandwidth, it is recommended you do the following beforehand in order to populate your local .m2 repo and Docker cache.

```
git clone https://github.com/gcharters/coffee.git
cd coffee/start/coffee-shop
mvn package
docker build -t masterclass:coffee-shop .
```
## The Application

The application consists of two Microservices; `coffee-shop` and `barista`.  The `coffee-shop` service allows you to place an order and the `barista` service services the making of the coffee.

```
                ^|
                || orderCoffee()
                || 
                || 
            ┌───|v────────┐   startCoffeeBrew()   ┌─────────────┐
            │ coffee-shop │---------------------->│   barista   │
            └─────────────┘<----------------------└─────────────┘
```
The completed code for the Masterclass is provided in the `coffee/finish` directory.  To work through the Masterclass you will develop in the `coffee/start` directory.


## Module 1: Build

Liberty has support for building and deploying applications using Maven and Gradle.  The source and documentation for these plugins can be found here:
* https://github.com/wasdev/ci.maven
* https://github.com/wasdev/ci.gradle

The Masterclass will make use of the `liberty-maven-plugin`.

Take a look at the maven build file for the coffee-shop project: `coffee/start/barista/pom.xml`

Go to the barista project:

```
cd coffee/start/barista
```

Build and run the barista service:

```
mvn install liberty:run
```

Visit: http://localhost:9081/openapi/ui

This page is an OpenAPI UI that lets you try out the barista service.  

Click on `POST` and then `Try it out`

Under `Example Value` specify:

```JSON
{
  "type": "ESPRESSO"
}
```

Click on `Execute`

Scroll down and you should see the server response code of `201`.  This says that the barista request to make an `ESPRESSO` was successfully `Created`.


## Module 2: Feature-based Build

The `liberty-maven-plugin` lets you specify which Liberty features you want to build against.

Take a look at the maven build file for the coffee-shop project: `coffee/start/coffee-shop/pom.xml`

In order for the plugin to know what features are available, we need to tell it where to find the feature information.  This is done with the following `<dependencyManagement/>` section:

```XML
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.openliberty.features</groupId>
                <artifactId>features-bom</artifactId>
                <version>RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```
We can now specify which features we want to build against.  

In the same `coffee-shop/pom.xml` locate the `<dependencies/>` section.  You'll see, for example, that we're depending on `jaxrs-2.1` because we're using this feature to implement the REST service:

``` XML
    <dependencies>
      <!--Open Liberty features -->
        <dependency>
            <groupId>io.openliberty.features</groupId>
            <artifactId>jaxrs-2.1</artifactId>
            <type>esa</type>
            <scope>provided</scope>
        </dependency>
        ...
    </dependencies>
```

Let's add add dependency on the `MicroProfile OpenAPI` feature so we can try the `coffee-shop` service out.

Add the following dependency to the `coffee-shop/pom.xml`

```XML
        <dependency>
            <groupId>io.openliberty.features</groupId>
            <artifactId>mpOpenAPI-1.0</artifactId>
            <type>esa</type>
            <scope>provided</scope>
        </dependency> 
```
The above dependency will cause the feature to be installed during the build, but we also need to tell the server to load it at runtime.

Open the file `coffee/start/coffee-shop/src/main/liberty/config/server.xml`

This file is the configuration for the `coffee-shop` server.

Near the top of the file, you'll see the following `<featureManager/>` entry:

```XML
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>ejbLite-3.2</feature>
        <feature>cdi-2.0</feature>
        <feature>beanValidation-2.0</feature>
        <feature>mpHealth-1.0</feature>
        <feature>mpConfig-1.3</feature>
        <feature>mpRestClient-1.1</feature>
        <feature>jsonp-1.1</feature>
    </featureManager>
```
This entry lists all the features to be loaded by the server.  Add the following entry inside the `<featureManager/>` element:

```XML
        <feature>mpOpenAPI-1.0</feature>
```

Build and run the coffee-shop service:

```
mvn install liberty:run
```

Visit: http://localhost:9080/openapi/ui

As with the barista service, this is an Open API UI page that lets to try out the service API for the coffee-shop service.

For a full list of all the features available, see https://openliberty.io/docs/ref/feature/.

## Module 3: Application APIs

Open Liberty has support for many standard APIs out of the box, including all the latest Java EE 8 APIs and the latest MicroProfile APIs.  To lead in the delivery of new APIs, a new version of Liberty is released every 4 weeks and aims to provide MicroProfile implementations soon after they are finalized.

As we've seen, to use a new feature, we need to add them to the build.  There is no need to add a dependency on the APIs for the feature because each feature depends on the APIs.  That means during build, the API dependencies are automatically added from maven central.

For example, take a look at: https://search.maven.org/artifact/io.openliberty.features/mpMetrics-1.1/18.0.0.4/esa

You'll see in the XML on the left that this feature depends on:

```XML
    <dependency>
      <groupId>io.openliberty.features</groupId>
      <artifactId>com.ibm.websphere.appserver.org.eclipse.microprofile.metrics-1.1</artifactId>
      <version>18.0.0.4</version>
      <type>esa</type>
    </dependency>
```
Which depends on the Metrics API from Eclipse MicroProfile:

```XML
    <dependency>
      <groupId>org.eclipse.microprofile.metrics</groupId>
      <artifactId>microprofile-metrics-api</artifactId>
      <version>1.1.1</version>
    </dependency>
```

And so during build, this API will be added for you.

We're now going to add Metrics to the `coffee-shop`.  Edit the `coffee/start/coffee-shop/pom.xml` file and add the following dependency:

```XML
        <dependency>
            <groupId>io.openliberty.features</groupId>
            <artifactId>mpMetrics-1.1</artifactId>
            <type>esa</type>
            <scope>provided</scope>
        </dependency>
```

Build the project:

```
mvn install
```

You should see that during the build, the following features are installed, and include mpMetrics-1.1:

```
[INFO] The following features have been installed: mpConfig-1.3 jndi-1.0 ejbLite-3.2 el-3.0 beanValidation-2.0 cdi-2.0 servlet-3.1 json-1.0 cdi-1.2 mpHealth-1.0 jaxrsClient-2.0 jsonp-1.0 mpRestClient-1.1 jsonp-1.1 servlet-4.0 jaxrsClient-2.1 jaxrs-2.1 ssl-1.0 distributedMap-1.0 mpConfig-1.2 mpMetrics-1.1 jaxrs-2.0 mpOpenAPI-1.0 appSecurity-2.0 

```
Now we have the API available, we can update the application to include a metric which will count the number of times a coffee order is requested. In the file `coffee/start/coffee-shop/src/main/java/com/sebastian_daschner/coffee_shop/boundary/OrdersResource.java`, add the following `@Counted` annotation to the `orderCoffee` method:

```Java
    @POST
    @Counted(name="order", displayName="Order count", description="Number of times orders requested.", monotonic=true)
    public Response orderCoffee(@Valid @NotNull CoffeeOrder order) {
        ...
    }
```

You'll also need to add the following package import:
```Java
import org.eclipse.microprofile.metrics.annotation.Counted;
```


Rebuild the project:

```
mvn install
```

## Module 4: Server Configuration

In the previous module you added the `mpMetrics-1.1` feature to the Liberty build.  This makes the feature available for use by the Liberty runtime, but as we saw with the `mpOpenAPI` feature loading the feature at runtime is a separate explicit choice.

Open the file `coffee/start/coffee-shop/src/main/liberty/config/server.xml`

Near the top of the file, you'll see the following `<featureManager/>` entry:

```XML
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>ejbLite-3.2</feature>
        <feature>cdi-2.0</feature>
        <feature>beanValidation-2.0</feature>
        <feature>mpHealth-1.0</feature>
        <feature>mpConfig-1.3</feature>
        <feature>mpRestClient-1.1</feature>
        <feature>jsonp-1.1</feature>
        <feature>mpOpenAPI-1.0</feature>
    </featureManager>
```

Add the following inside the `<featureManager/>` element to include the `mpMetrics-1.1` feature:

```XML
        <feature>mpMetrics-1.1</feature>
```

In the `coffee/start/coffee-shop` directory, build the updated application and start the server:

```
mvn install liberty:run
```

You should now see a message for a new metrics endpoint that looks like:

```
[INFO] [AUDIT   ] CWWKT0016I: Web application available (default_host): http://localhost:9080/metrics/

```

Open the metrics endpoint in your browser.  You should see a message like this:

```
Error 403: Resource must be accessed with a secure connection try again using an HTTPS connection.
```

If you take a look at the server output, you should see the following error:

```
[INFO] [ERROR   ] CWWKS9113E: The SSL port is not active. The incoming http request cannot be redirected to a secure port. Check the server.xml file for configuration errors. The https port may be disabled. The keyStore element may be missing or incorrectly specified. The SSL feature may not be enabled.
```

It's one thing to configure the server to load a feature, but many Liberty features require additional configuration.  The complete set of Liberty features and their configuration can be found here: https://openliberty.io/docs/ref/config/.

The error message suggests we need to add a `keyStore` and one route to solve this would be to add a `keyStore` and user registry (e.g. a `basicRegistry` for test purposes).  However, if we take a look at the configuration for mpMetrics (https://openliberty.io/docs/ref/config/#mpMetrics.html) we can see that it has an option to turn the metrics endpoint authentication off.

Add the following to the `coffee/start/coffee-shop/src/main/liberty/config/server.xml`

```XML
    <mpMetrics authentication="false" />
```

Rebuild, restart the server and visit the metrics endpoint, you should see a number of metrics automatically generated by the JVM:

```
 TYPE base:classloader_total_loaded_class_count counter
# HELP base:classloader_total_loaded_class_count Displays the total number of classes that have been loaded since the Java virtual machine has started execution.
base:classloader_total_loaded_class_count 10616
...
```
This doesn't contain the metrics you added because the service hasn't been called and so no application metrics have been recorded. Use the OpenAPI UI to send a few requests to the service and reload the metrics page.  At the bottom of the metrics you should see:

```
...
# TYPE application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order counter
# HELP application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order Number of times orders requested.
application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order 3
```

## Module 5: Externalizing Configuration

If you're familiar with the concept of 12-factor applications (see http://12factor.net) you'll know that factor III states that an applications configuration should be stored in the environment.  Config here is referring to things which vary between development, staging and production. In doing so you can build the deployment artefact once and deploy it in the different environments unchanged.

Liberty lets your application pick up configuration from a number of sources, such as environment variables, bootstrap.properties and Kubernetes configuration.

Bootstrap.properties lets you provide simple configuration values to substitute in the server configuration and also to use within the application.  The following example replaces the hard-coded base URL the `coffee-shop` service uses to talk to the `barista` service, as well as the ports it exposes.

In the `coffee/start/coffee-shop/pom.xml` file, in the `<properties/>` add:

```XML
    <properties>
        ...
        <testServerHttpPort>9080</testServerHttpPort>
        <testServerHttpsPort>9443</testServerHttpsPort>
        <baristaBaseURL>http://localhost:9081</baristaBaseURL>
        ...
    </properties>
```
This `<properties/>` element is where the property values are set that can then be re-used within the maven project.  

In the `<bootstrapProperties/>` section of the `liberty-maven-plugin` configuration, add the following:

```XML
                    <bootstrapProperties>
                        ...
                        <env.default_http_port>${testServerHttpPort}</env.default_http_port>
                        <env.default_https_port>${testServerHttpsPort}</env.default_https_port>
                        <default_barista_base_url>${baristaBaseURL}</default_barista_base_url>
                    </bootstrapProperties>
```
The above takes the properties we defined in the maven project and passes them to Liberty as bootstrap properties.

Note, we're using the `env.` prefix because in the Docker modules of this Masterclass you will set these through environment variables. Note, also the names use underscores (`_`) so they can be passed as environment variables.

Build the project:

```
mvn install
```
The `liberty-maven-plugin` generated the following file `target/liberty/wlp/usr/servers/defaultServer/bootstrap.properties` which contains the configuration that will be loaded and applied to the server configuration.  If you view the file you'll see the values you specified:

```YAML
# Generated by liberty-maven-plugin
default_barista_base_url=http://localhost:9081
env.default_http_port=9080
env.default_https_port=9443
war.name=coffee-shop.war
```
We now need to change the server configuration to use these values.  In the `coffee/start/coffee-shop/src/main/liberty/config/server.xml` file, change this line:

```XML
    <httpEndpoint host="*" httpPort="9080" httpsPort="9443" id="defaultHttpEndpoint"/>
```
to 

```XML
    <httpEndpoint host="*" httpPort="${env.default_http_port}" httpsPort="${env.default_https_port}" id="defaultHttpEndpoint"/>
```

Next we'll use the `default_barista_base_url` in the code to avoid hard-coding the location of the `barista` service.

Edit the file `coffee/start/coffee-shop/src/main/java/com/sebastian_daschner/coffee_shop/control/Barista.java`

Change:

```Java
    String baristaBaseURL = "http://localhost:9081";
```

To:

```Java
    @Inject
    @ConfigProperty(name="default_barista_base_url")
    String baristaBaseURL;
```
You'll also need to add the following imports:

```Java
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
```
This is using the MicroProfile Config specification to inject the configuration value.  Configuration can come from a number of sources, including `bootstrap.properties`.

We also need to make the same changes to the CoffeeShopHealth of the `coffee-shop` service. Edit the file: `coffee/start/coffee-shop/src/main/java/com/sebastian_daschner/coffee_shop/boundary/CoffeeShopHealth.java`

Change:

```Java
    String baristaBaseURL = "http://localhost:9081";
```

To:

```Java
    @Inject
    @ConfigProperty(name="default_barista_base_url")
    String baristaBaseURL;
```
Add the following imports:

```Java
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
```

For more information on MicroProfile Config see https://openliberty.io/guides/microprofile-config.html.

Rebuild the code, start the `coffee-shop` and `barista` servers and try out the endpoint through the Open API UI.  You can also try out the health endpoint at `http://localhost:9080/health`.

## Module 6: Integration Testing

Tests are essential for developing maintainable code.  Developing your application using bean-based component models like CDI makes your code easily unit-testable. Integration Tests are a little more challenging.  In this section you'll add a `barista` service integration test using the `maven-failsafe-plugin`.  During the build, the Liberty server will be started along with the `barista` application deployed, the test will be run and then the server will be stopped.  The starting and stopping of the Liberty server is configured by the Liberty parent pom (see https://search.maven.org/artifact/net.wasdev.wlp.maven.parent/liberty-maven-app-parent/2.6.3/pom), which is configured as the parent of the Masterclass poms.

Because we're going to be testing a REST `POST` request, we need JAX-RS client support and also support for serializing `json` into the request.  We also need `junit` for writing the test.  Add these dependencies to the `coffee/start/barista/pom.xml`:

```XML
        <!-- Test dependencies -->  
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.cxf</groupId>
            <artifactId>cxf-rt-rs-mp-client</artifactId>
            <version>3.3.0</version>
            <scope>test</scope>
        </dependency>     
        <dependency>
            <groupId>com.fasterxml.jackson.jaxrs</groupId>
            <artifactId>jackson-jaxrs-json-provider</artifactId>
            <version>2.9.3</version>
            <scope>test</scope>
        </dependency>
```

Note the `<scope/>` of the tests is set to `test` because we only want the dependencies to be used during testing.

Next add `maven-failsafe-plugin` configuration at the end of the `<plugins/>` section:

```XML
        <plugins>
            ...
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>2.19.1</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                        <configuration>
                            <systemPropertyVariables>
                                <liberty.test.port>${testServerHttpPort}</liberty.test.port>
                            </systemPropertyVariables>
                        </configuration>
                    </execution>
                </executions>
            </plugin>            
        </plugins>
```
Note, this configuration makes the port of the server available to the test as a system property called `liberty.test.port`.

Finally, add the test code.  Create a file called, `coffee/start/barista/src/test/java/com/sebastian-daschner/barista/it/BaristaIT.java` and add the following:

```Java
package com.sebastian_daschner.barista.it;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import com.sebastian_daschner.barista.entity.CoffeeBrew;
import com.sebastian_daschner.barista.entity.CoffeeType;

public class BaristaIT {
    private static String URL;

    @BeforeClass
    public static void init() {
        String port = System.getProperty("liberty.test.port");
        URL = "http://localhost:" + port + "/barista/resources/brews";
    }
    @Test
    public void testService() throws Exception {

        Client client = null;
        WebTarget target = null;
        try {
            client = ClientBuilder.newClient().register(JacksonJsonProvider.class);
            target = client.target(URL);

        } catch (Exception e) {
            client.close();
            throw e;
        }

        CoffeeBrew brew = new CoffeeBrew();
        brew.setType(CoffeeType.POUR_OVER);

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(brew));

        try {
            if (response == null) {
                assertNotNull("GreetingService response must not be NULL", response);
            } else {
                assertEquals("Response must be 200 OK", 200, response.getStatus());
            }

        } finally {
            response.close();
        }
    }
}
```

This test sends a `json` request to the `barista` service and checks for a `200 OK` response. 

Re-build and run the tests:

```
mvn install
```

In the output of the build, you should see:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.sebastian_daschner.barista.it.BaristaIT
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.365 sec - in com.sebastian_daschner.barista.it.BaristaIT

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

## Module 7: Docker

We're now going to dockerize the two services and show how we can override the defaults to re-wire the two services.  We're going to use a Docker user-defined network (see https://docs.docker.com/network/network-tutorial-standalone/#use-user-defined-bridge-networks) because we'll be running them on the same host and it keeps things simple.  For real-world production deployments you would use a Kubernetes environment, such as IBM Cloud Private or the IBM Cloud Kubernetes Service.

Take a look at the `coffee/start/coffee-shop/Dockerfile`:

```Dockerfile
FROM open-liberty:kernel-java8-ibm
ADD /target/coffee-shop.tar.gz /opt/ol
RUN rm /opt/ol/wlp/usr/servers/defaultServer/bootstrap.properties
EXPOSE 9080 9443
```
The `FROM` statement is building this image using the Open Liberty kernel image (see https://hub.docker.com/_/open-liberty/ for the available images).  The `ADD` statement unzips our packaged application and server configuration, the `RUN` removes the `bootstrap.properties` file to avoid accidentally using it and avoid conflicts with the environment variables we will pass in later through Docker, and the `EXPOSE` makes the two server ports available outsides the container.

Let's build the docker image.  In the `coffee/start/coffee-shop` directory, run:

```
docker build -t masterclass:coffee-shop .
```

In the `coffee/start/barista` directory, run:

```
docker build -t masterclass:barista .
```
Next, create the user-defined bridge network:

```
docker network create --driver bridge masterclass-net
```
You can now run the two Docker containers and get them to join the same bridge network.  Providing names to the containers makes those names available for DNS resolution within the bridge network so there's no need to use ip addresses.

Run the `barista` container:

```
docker run --network=masterclass-net --name=barista masterclass:barista
```

Note, we don't need map the `barista` service ports outside the container because the bridge network gives access to the other containers on the same network.

Next, we're going to run the `coffee-shop` container.  For it to work we'll need to provide new values for ports and the location of the barista service.  Run the `coffee-shop` container

```
docker run -p 9080:9080 -p 9445:9443 --network=masterclass-net --name=coffee-shop -e default_barista_base_url='http://barista:9081' -e default_http_port=9080 -e default_https_port=9443 masterclass:coffee-shop
```

You can take a look at the bridge network using:

```
docker network inspect masterclass-net
```
You'll see something like:

```JSON
[
    {
        "Name": "masterclass-net",
        ...
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.19.0.0/16",
                    "Gateway": "172.19.0.1"
                }
            ]
        },
        ...
        "Containers": {
            "0fc740d52f2ed8dfdb04127fe3e49366dcbeb7924fee6b0cbf6f891c0909b0e8": {
                "Name": "coffee-shop",
                "EndpointID": "157d697fb4bff2722d654c68e3a5e5fe7554a91e860213d22362cd7cc074fc8f",
                "MacAddress": "02:42:ac:13:00:02",
                "IPv4Address": "172.19.0.2/16",
                "IPv6Address": ""
            },
            "2b78ebf13596147042c8f2f5bd3171ca1c6f77241f419472010ddc2f28fd7a0c": {
                "Name": "barista",
                "EndpointID": "c93163547eb7e3c2c84dd0f72beb77127cfc319b6d9d7f6d9d99e17b85ff6d30",
                "MacAddress": "02:42:ac:13:00:03",
                "IPv4Address": "172.19.0.3/16",
                "IPv6Address": ""
            }
        },
        "Options": {},
        "Labels": {}
    }
]
```
If you need to remove a container, use:

```
docker container rm <container name>
```
You should now be able to loads the `coffee-shop` service's Open API page and call the service.  Give it a try.

### Overriding Dev Server Configuration

The above works fine, but still has a metrics endpoint with authentication turned off.  We'll now show how `configDropins/overrides` can be used to override existing, or add new, server configuration.  For example, this can be used to add server configuration in a production environment.  The approach we're going to take is to use a Docker volume, but in a real-world scenario you would use Kubernetes ConfigMaps to include the production server configuration, or build a new image based on the dev image that adds the production configuration.  Whichever approach is taken, separating the dev, staging and prod configuration into separate server configuration files is the goal.

Take a look at the file `coffee/start/coffee-shop/configDropins/overrides/metrics-prod.xml`:

```XML
<?xml version="1.0" encoding="UTF-8"?>
<server description="Coffee Shop Server">

    <featureManager>
        <feature>mpMetrics-1.1</feature>
    </featureManager>
    
    <mpMetrics authentication="true" />

     <!-- 
     Note, this configuration is for demo purposes
     only and MUST NOT BE USED IN PRODUCTION AS IT 
     IS INSECURE. -->  
    <variable name="admin.password" value="change_it" />
    <variable name="keystore.password" value="change_it" />
    
    <quickStartSecurity userName="admin" userPassword="${admin.password}"/>
    <keyStore id="defaultKeyStore" password="${keystore.password}"/>    
     
</server>
```
You'll see that this turns metrics authentication on and sets up some simple security required for securing/accessing the metrics endpoint.  Note, this configuration really is **NOT FOR PRODUCTION**, it's simply aiming to show how to override, or provide new, server configuration.

If you're on a unix-based OS, in the `coffee/start/coffee-shop`directory, run the `coffee-shop` container:

```
docker run -p 9080:9080 -p 9445:9443 --network=masterclass-net --name=coffee-shop -e default_barista_base_url='http://barista:9081' -e default_http_port=9080 -e default_https_port=9443 -v $(pwd)/configDropins/overrides:/opt/ol/wlp/usr/servers/defaultServer/configDropins/overrides  masterclass:coffee-shop
```
The above relies on `pwd` to fill in the docker volume source path.  If you're on Windows, replace `$(pwd)` with the absolute path to the `coffee/start/coffee-shop` directory in the above command.

You should see the following message as the server is starting:

```
[AUDIT   ] CWWKG0102I: Found conflicting settings for mpMetrics configuration.
  Property authentication has conflicting values:
    Value false is set in file:/opt/ol/wlp/usr/servers/defaultServer/server.xml.
    Value true is set in file:/opt/ol/wlp/usr/servers/defaultServer/configDropins/overrides/metrics-prod.xml.
  Property authentication will be set to true.
```
This shows that we have turned metrics authentication back on.

Access the metrics endpoint at: `https://localhost:9445/metrics`

You will see that the browser complains about the certificate.  This is a self-signed certificate generated by Liberty for test purposes.  Accept the exception (note,  Firefox may not allow you to do this in which case you'll need to use a different browser).  You'll be presented with a login prompt.  Sign in with userid `admin` and password `change_it` (the values in the `metrics-prod.xml`).


## Module 8: Support Licensing

Open Liberty is Open Source under the Eclipse Public License v1, as a result there is no fee to use in production.  Community support is available via StackOverflow or the mail list, and bugs can be raised in github (https://github.com/openliberty/open-liberty).  Commercial support from IBM is available for Open Liberty, you can find out more on the IBM Marketplace. The WebSphere Liberty product is built on Open Liberty, there is no migration required to use WebSphere Liberty, you simply point to WebSphere Liberty in your build.  Users of WebSphere Liberty get support for the packaged Open Liberty function.

WebSphere Liberty is also available in Maven Central - see https://search.maven.org/search?q=g:com.ibm.websphere.appserver.runtime

You can use WebSphere Liberty for development even if you haven't purchased it, but if you have production entitlement you can easily change to use it, as follows:

In the `coffee/start/coffee-shop/pom.xml` change these two lines from:

```XML
                        <groupId>io.openliberty</groupId>
                        <artifactId>openliberty-kernel</artifactId>
```
To:
```XML
                        <groupId>com.ibm.websphere.appserver.runtime</groupId>
                        <artifactId>wlp-kernel</artifactId>
```
Rebuild and re-start the `coffee-shop` service:

```
mvn install liberty:run
```

Try the service out using the Open API Web page and you should see the behavior is identical.  Not surprising since the code is identical, from the same build, just built into WebSphere Liberty.

## Conclusion

Thanks for trying the Open Liberty Materclass. If you're interested in finding out more, please visit http://openliberty.io, and for more hands-on esperience, why not try the Open Liberty Guides - http://openliberty.io/guides.



