FROM clojure:openjdk-8-lein-2.8.3

WORKDIR /usr/src/app

RUN curl -sL https://deb.nodesource.com/setup_11.x | bash -
RUN apt-get update && apt-get install --yes build-essential nodejs

COPY project.clj /usr/src/app
RUN lein deps

COPY . /usr/src/app
RUN make

CMD ["lein", "run"]
