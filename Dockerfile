FROM clojure:openjdk-8-lein-2.8.3
WORKDIR /usr/src/app
COPY project.clj /usr/src/app
RUN lein deps
RUN make
COPY . /usr/src/app
CMD ["lein", "run"]
