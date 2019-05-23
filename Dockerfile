FROM hseeberger/scala-sbt as builder
ARG PROJECT
WORKDIR /build
COPY project project
COPY build.sbt .
RUN sbt update
COPY . .
RUN sbt $PROJECT/universal:packageBin

FROM openjdk:8u181-jre-slim
ARG VERSION
ARG PROJECT
ENV version=$VERSION
ENV name=compendium-$PROJECT
COPY --from=builder /build/modules/$PROJECT/target/universal/. .
RUN unzip -o ./${name}-$VERSION.zip
RUN chmod +x ${name}-$VERSION/bin/$name
ENTRYPOINT ${name}-$version/bin/$name
