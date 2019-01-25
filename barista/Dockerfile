FROM open-liberty:kernel-java8-ibm
ADD /target/barista.tar.gz /opt/ol
RUN rm /opt/ol/wlp/usr/servers/defaultServer/bootstrap.properties
EXPOSE 9081 9444