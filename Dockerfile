FROM toposoid/scala-knp:2.13.11-4.19

WORKDIR /app
ARG TARGET_BRANCH
ENV DEPLOYMENT=local
ENV _JAVA_OPTIONS="-Xms512m -Xmx4g"

RUN apt-get update \
&& apt-get -y install git unzip \
&& git clone https://github.com/toposoid/scala-common.git \
&& cd scala-common \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-common.git \
&& cd toposoid-common \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-knowledgebase-model.git \
&& cd toposoid-knowledgebase-model \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/scala-juman-knp.git \
&& cd scala-juman-knp \
&& git checkout scala-2.12-support \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/toposoid-deduction-protocol-model.git \
&& cd toposoid-deduction-protocol-model \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
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
&& unzip -o toposoid-sentence-parser-japanese-web-0.5-SNAPSHOT.zip


COPY ./docker-entrypoint.sh /app/
ENTRYPOINT ["/app/docker-entrypoint.sh"]
