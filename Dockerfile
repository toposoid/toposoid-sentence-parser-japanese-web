FROM toposoid/scala-knp:2.12.12-4.19

WORKDIR /app

ENV DEPLOYMENT=local
ENV _JAVA_OPTIONS="-Xms2g -Xmx4g"

RUN apt-get update \
&& apt-get -y install git unzip \
&& git clone https://github.com/toposoid/scala-common.git \
&& cd scala-common \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-common.git \
&& cd toposoid-common \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-knowledgebase-model.git \
&& cd toposoid-knowledgebase-model \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/scala-juman-knp.git \
&& cd scala-juman-knp \
&& git checkout scala-2.12-support \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-sentence-parser.git \
&& cd toposoid-sentence-parser \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-deduction-protocol-model.git \
&& cd toposoid-deduction-protocol-model \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-sentence-parser-web.git \
&& cd toposoid-sentence-parser-web \
&& sbt playUpdateSecret 1> /dev/null \
&& sbt dist \
&& cd /app/toposoid-sentence-parser-web/target/universal \
&& unzip -o toposoid-sentence-parser-web-0.1.0.zip


COPY ./docker-entrypoint.sh /app/
ENTRYPOINT ["/app/docker-entrypoint.sh"]
