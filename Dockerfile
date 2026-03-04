FROM toposoid/toposoid-scala-lib-base:0.7-SNAPSHOT
FROM toposoid/scala-knp:3.3.6-5.0
#WORKDIR /root
#RUN mkdir -p /root/.m2/repository
#COPY --from=0 /root/.ivy2/local ./.ivy2/local
#COPY --from=0 /root/.m2 ./.m2

WORKDIR /app
ARG TARGET_BRANCH
ENV DEPLOYMENT=local
ENV _JAVA_OPTIONS="-Xms2g -Xmx4g"

RUN apt-get update \
&& apt-get -y install git unzip \
&& git clone https://github.com/toposoid/scala-juman-knp.git \
&& cd scala-juman-knp \
&& git checkout scala-3.3.6-knp-5.0-support  \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-sentence-parser-japanese.git \
&& cd toposoid-sentence-parser-japanese \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-sentence-parser-japanese-web.git \
&& cd toposoid-sentence-parser-japanese-web \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt playUpdateSecret 1> /dev/null \
&& sbt dist \
&& cd /app/toposoid-sentence-parser-japanese-web/target/universal \
&& unzip -o toposoid-sentence-parser-japanese-web-0.7-SNAPSHOT.zip


COPY ./docker-entrypoint.sh /app/
ENTRYPOINT ["/app/docker-entrypoint.sh"]
