# Open Liberty Masterclass

This document contains the hands-on lab modules for the Open Liberty Masterclass.  It is intended to be used in conjunction with taught materials.

## Before you Begin

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
cd coffee/coffee-shop
mvn package
docker build -t masterclass:coffee-shop .
```

## Build

Liberty has support for building and deploying applications using Maven and Gradle.  The source and documentation for these plugins can be found here:
* https://github.com/wasdev/ci.maven
* https://github.com/wasdev/ci.gradle

The Masterclass will make use of the `liberty-maven-plugin`.

Take a look at the maven build file for the coffee-shop project: `coffee/barista/pom.xml`

Go to the barista project:

```
cd coffee/barista
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

Scroll down and you should see the server response code of `202`.  This says that the barista `Accepted` the request to make an `ESPRESSO`.


## Feature-based Build

The `liberty-maven-plugin` lets you specify which Liberty features you want to build against.

Take a look at the maven build file for the coffee-shop project: `coffee/coffee-shop/pom.xml`

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

Build and run the coffee-shop service:

```
mvn install liberty:run
```

Visit: http://localhost:9080/openapi/ui

As with the barista services, this is an Open API UI page that lets to try out the service API for the coffee-shop service.

For a full list of all the features available, see https://openliberty.io/docs/ref/feature/.

**TODO: Not clear what the purpose is.  As it stands, they can build but won't see anything different happen.  Should we make change this to add Open API?  But then we'll need to talk about server configuration to add the feature to the server.**

## Application APIs

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

We're now going to add Metrics to the `coffee-shop`.  Edit the `coffee/coffee-shop/pom.xml` file and add the following dependency:

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
[INFO] Installing features: [mpconfig-1.3, ejbLite-3.2, beanValidation-2.0, cdi-2.0, mpHealth-1.0, mprestclient-1.1, jsonp-1.1, ejblite-3.2, mpConfig-1.3, jaxrs-2.1, mpRestClient-1.1, mpMetrics-1.1, mpopenapi-1.0, mpOpenAPI-1.0, beanvalidation-2.0, mphealth-1.0]

```


## Server Configuration

In the previous module you added the `mpMetrics-1.1` feature to the Liberty build.  This makes the feature available for use in the runtime, but actually loading the feature at runtime is a separate explicit choice.

Open the file `coffee/coffee-shop/src/main/liberty/config/server.xml`

This file is the configuration for the `coffee-shop` server.

Near the top of the file, you'll see the following `<featureManager/>` entry:

```XML
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>ejbLite-3.2</feature>
        <feature>cdi-2.0</feature>
        <feature>beanValidation-2.0</feature>
        <feature>mpOpenAPI-1.0</feature>
        <feature>mpHealth-1.0</feature>
        <feature>mpConfig-1.3</feature>
        <feature>mpRestClient-1.1</feature>
        <feature>jsonp-1.1</feature>
    </featureManager>
```

This entry lists all the features to be started by the server.  Add the following inside the `<featureManager/>` element to include the `mpMetrics-1.1` feature:

```XML
        <feature>mpMetrics-1.1</feature>
```

Next we'll update the code to include a metric which will count the number of times a coffee order is requested. In the file `coffee/coffee-shop/src/main/java/com/sebastian_daschner/coffee_shop/boundary/OrdersResource.java`, add the following `@Counted` annotation to the `orderCoffee` method:

```Java
    @POST
    @Counted(name="order", displayName="Order count", description="Number of times orders requested.", monotonic=true)
    public Response orderCoffee(@Valid @NotNull CoffeeOrder order) {
        ...
    }
```

In the `coffee/coffee-shop` directory, build the updated application and start the server:

```
mvn install liberty:run
```

You should now see a new metrics endpoint that looks like:

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

The error message suggests we need to add a `keyStore` and one route to solve this would be to add a `keyStore` and user registry (e.g. a `basicRegistry` for test purposes).  However, if we take a look at the configuration for mpMetrics (https://openliberty.io/docs/ref/config/#mpMetrics.html) we can see that it has an option to turn the metrics endpoint security off.

Add the following to the `coffee/coffee-shop/src/main/liberty/config/server.xml`

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
This doesn't contain the metrics you added because the service hasn't been called. Use the OpenAPI UI to send a few requests to the service reload the metrics page.  At the bottom of the metrics you should see:

```
...
# TYPE application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order counter
# HELP application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order Number of times orders requested.
application:com_sebastian_daschner_coffee_shop_boundary_orders_resource_order 3
```

## Externalizing Configuration

If you're familiar with the concept of 12-factor applications (see http://12factor.net) you'll know that factor III states that an applications configuration should be stored in the environment.  Config here is referring to things which vary between development, staging and production. In doing so you can build the deployment artefact once and deploy it in the different environments unchanged.

Liberty lets your application pick up configuration from a number of sources, such as environment variables, bootstrap.properties and Kubernetes configuration.

Bootstrap.properties lets you provide simple configuration values to substitute in the server configuration and also to use within the applications.  The following example replaces the hard-coded port the `coffee-shop` service uses to talk to the `barista`, as well as the ports it exposes.

In the `coffee/coffee-shop/pom.xml` file, in the `<properties/>` add:

```XML
    <properties>
        ...
        <testServerHttpPort>9080</testServerHttpPort>
        <testServerHttpsPort>9443</testServerHttpsPort>
        <baristaHttpPort>9081</baristaHttpPort>
        ...
    </properties>
```
This sets the property values that can then be re-used within the maven project.  

In the `<bootstrapProperties/>` section of the `liberty-maven-plugin` configuration, add the following:

```XML
                    <bootstrapProperties>
                        <default.http.port>${testServerHttpPort}</default.http.port>
                        <default.https.port>${testServerHttpsPort}</default.https.port>
                        <default.barista.http.port>${baristaHttpPort}</default.barista.http.port>
                    </bootstrapProperties>
```
The above takes the properties we defined in the maven project and passes them to liberty as bootstrap properties.

Build the project:

```
mvn install
```
The `liberty-maven-plugin` generated the following file `target/liberty/wlp/usr/servers/defaultServer/bootstrap.properties` which contains the configuration that will be loaded and applied to the server configuration.  If you view the file you'll see the values you specified:

```YAML
# Generated by liberty-maven-plugin
default.barista.http.port=9081
default.http.port=9080
default.https.port=9443
war.name=coffee-shop.war
```
We now need to change the server configuration to use these values.  In the `coffee/coffee-shop/src/main/liberty/config/server.xml` file, change this line:

```XML
    <httpEndpoint host="*" httpPort="9080" httpsPort="9443" id="defaultHttpEndpoint"/>
```
to 

```XML
    <httpEndpoint host="*" httpPort="${default.http.port}" httpsPort="${default.https.port}" id="defaultHttpEndpoint"/>
```
Next we'll use the `default.barista.http.port` in the code to avoid hard-coding the port number.

Edit the file `coffee/coffee-shop/src/main/java/com/sebastian_daschner/coffee_shop/control/Barista.java`

Change:

```Java
    String baristaHttpPort = "9081";
```

To:

```Java
    @Inject
    @ConfigProperty(name="default.barista.http.port")
    String baristaHttpPort;
```
You'll also need to add the following imports:

```Java
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
```
This is using the MicroProfile Config specification to inject the configuration value.  Configuration can come from a number of sources, including `bootstrap.properties`.

For more information on MicroProfile Config see https://openliberty.io/guides/microprofile-config.html.

Rebuild the code, start the server and try out the endpoint through the Open API UI.

## Docker

We're now going to dockerize the two services and show how we can override the defaults to re-wire the two services.  We're going to use a Docker user-defined network (see https://docs.docker.com/network/network-tutorial-standalone/#use-user-defined-bridge-networks) because we'll be running them on the same host and it keeps things simple.  For real-world production deployments you would use a Kubernetes environment, such as IBM Cloud Private or the IBM Cloud Kubernetes Service.

Take a look at the `coffee/coffee-shop/Dockerfile`:

```Dockerfile
FROM open-liberty:kernel-java8-ibm
ADD /target/coffee-shop.tar.gz /opt/ol
RUN rm /opt/ol/wlp/usr/servers/defaultServer/bootstrap.properties
EXPOSE 9080 9443
```
The `FROM` statement is building this image using the Open Liberty kernel image (see https://hub.docker.com/_/open-liberty/ for the available images).  The `ADD` statement unzips our packaged application and server configuration, the `RUN` removes the `bootstrap.properties` file to avoid accidentally using it, and the `EXPOSE` makes the two server ports available outsides the container.

Let's build the docker images.  In the `coffee/coffee-shop` directory, run:

```
docker build -t masterclass:coffee-shop .
```

In the `coffee/barista` directory, run:

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

Run the `coffee-shop` container:

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








## Support Licensing

