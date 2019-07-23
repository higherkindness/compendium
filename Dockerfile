FROM hseeberger/scala-sbt:8u212_1.2.8_2.12.8 as builder
ARG PROJECT
WORKDIR /build
COPY project project
COPY build.sbt .
RUN sbt update
COPY . .
RUN sbt $PROJECT/universal:packageZipTarball

FROM openjdk:8u212-jre-slim
ARG VERSION
ARG PROJECT
ENV version=$VERSION
ENV name=compendium-$PROJECT
COPY --from=builder /build/modules/$PROJECT/target/universal/. .
RUN tar -zxvf ./${name}-$VERSION.tgz
RUN chmod +x ${name}-$VERSION/bin/$name
ENTRYPOINT ${name}-$version/bin/$name
