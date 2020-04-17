FROM hseeberger/scala-sbt:8u242_1.3.10_2.13.1 as builder
WORKDIR /build
COPY . .
RUN sbt universal:packageZipTarball

FROM openjdk:8u242-jre-slim
ARG VERSION
ENV version=$VERSION
ENV name=compendium-server
COPY --from=builder /build/target/universal/. .
RUN tar -zxvf ./${name}-$VERSION.tgz
RUN chmod +x ${name}-$VERSION/bin/$name
ENTRYPOINT ${name}-$version/bin/$name
