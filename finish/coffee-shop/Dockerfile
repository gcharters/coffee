FROM open-liberty:kernel-java8-ibm
ADD /target/coffee-shop.tar.gz /opt/ol
RUN rm /opt/ol/wlp/usr/servers/defaultServer/bootstrap.properties
EXPOSE 9080 9443