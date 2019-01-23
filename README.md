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

Note, the password here has not been hashed which is not a good practice.  Liberty has support for creating and including hashed passwords for greater security. 

Rebuild, restart the server and visit the metrics endpoint.

You should now be redirected to the secure endpoint, which will look something like:

```
https://localhost:9443/metrics/
```

The browser will complain about the certificate because it has been generated and self-signed, rather than coming from a certificate authority.  Liberty generates the certification for you to help test out https endpoints, but you shouldn't use this in production.

Continue to the metrics page (note, Firefox may not let you continue, in which case you may need to use a different browser).

You will be presented with a sign-in prompt.  We haven't configured a user registry so don't actually have any credentials we can use to get to the page.




## Externalizing Configuration

## Module 1: Runtime Features

**TODO: Move this all somewhere else.  It requires too many concepts that haven't been introduced. **

Liberty lets you use just enough runtime to support your application.  In this module you will start the server and inspect what is running and why.

Go to the barista project:

```
cd coffee/barista
```

Build and run the barista project (you'll learn more about building Liberty in later modules):

```
mvn install
```

Look at the server configuration: `barista/src/main/liberty/config/server.xml`

You will see the following which tells Liberty which features to load:

```XML
    <featureManager>
        <feature>jaxrs-2.1</feature>
        <feature>cdi-2.0</feature>
        <feature>mpOpenAPI-1.0</feature>
        <feature>mpHealth-1.0</feature>
    </featureManager>
```
In the build output you will see the following, which identifies which features are being installed:

```
[INFO] Installing features: [cdi-2.0, mpopenapi-1.0, mpHealth-1.0, mpOpenAPI-1.0, mphealth-1.0, jaxrs-2.1]
```
Ignoring duplicate names, you will see that this set matches the set in the server.xml.

Now run the server:

```
mvn liberty:run
```

Look at the output of your running server and you will see the following message:

```
[INFO] [AUDIT   ] CWWKF0012I: The server installed the following features: [servlet-4.0, jndi-1.0, cdi-2.0, mpHealth-1.0, json-1.0, mpOpenAPI-1.0, jsonp-1.1, mpConfig-1.2, jaxrsClient-2.1, jaxrs-2.1].
```

This tells you which features are loaded.  There are more features in this list than were specified in the `server.xml`.  Features can depend on other features, and Liberty has determined that in order to load features specified in the server configuration, it requires a number of additional features.  For example, `jaxrs-2.1` is built using `servlet-4.0`.

## Docker

## Support Licensing

