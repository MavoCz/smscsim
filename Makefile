.RECIPEPREFIX +=

.env:
  cp .env.example .env

.PHONY: test
test:
  docker-compose run --rm -v "$$HOME/.m2":/root/.m2  maven mvn clean test
  docker-compose run --rm -v "$$HOME/.m2":/root/.m2  maven mvn clean #workaround fo removing write-protected `build/*` created by root user inside container

.PHONY: build
build:
  docker-compose run --rm -v "$$HOME/.m2":/root/.m2  maven mvn -Dmaven.test.skip=true package
  docker build -f Dockerfile -t smscsim:latest .
  docker image prune -f

.PHONY: run
run: .env
  docker run --rm --detach --network=host --env-file .env --name smscsim smscsim

.PHONY: logs
logs:
  docker logs -f smscsim

.PHONY: start
start:
  $(MAKE) build
  $(MAKE) run

.PHONY: stop
stop:
  docker stop smscsim

.PHONY: restart
restart:
  $(MAKE) stop
  $(MAKE) start