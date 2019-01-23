FROM open-liberty:microProfile2-java8-ibmsfj as server-setup
ADD /target/coffee-shop.tar.gz /config/
RUN rm -rf /config/wlp/usr/*
#COPY /target/coffee-shop.zip /config/
#RUN apt-get update \
#    && apt-get install -y --no-install-recommends unzip
#RUN unzip /config/coffee-shop.tar.gz && \
#    mv /wlp/usr/servers/coffee-shopServer/* /config/ && \
#    rm -rf /config/wlp && \
#    rm -rf /config/coffee-shop.zip

#FROM open-liberty
#COPY --from=server-setup /config/ /config/
EXPOSE 9080 9443