.RECIPEPREFIX +=

DOCKER_IMAGE=smscsim

simulator:
  cp .env.$@ .env

messagebird:
  cp .env.$@ .env

.PHONY: test
test:
  mvn test

.PHONY: build
build:
  docker-compose run --rm -v "$$HOME/.m2":/root/.m2  maven mvn -Dmaven.test.skip=true package
  docker build -f Dockerfile -t smscsim:latest .
  docker image prune -f

.PHONY: run-simulator
run-simulator: simulator
  docker run --rm --detach --network=host --env-file .env --name $< $(DOCKER_IMAGE)

.PHONY: run-messagebird
run-messagebird: messagebird
  docker run --rm --detach --network=host --env-file .env --name $< $(DOCKER_IMAGE)

.PHONY: log-simulator
log-simulator:
  docker logs -f simulator

.PHONY: log-messagebird
log-messagebird:
  docker logs -f messagebird

.PHONY: start
start:
  $(MAKE) build
  $(MAKE) run-simulator
  $(MAKE) run-messagebird

.PHONY: start-simulator
start-simulator: build
 $(MAKE) run-simulator

.PHONY: start-messagebird
start-messagebird: build
 $(MAKE) run-messagebird

.PHONY: stop
stop:
  $(MAKE) stop-simulator
  $(MAKE) stop-messagebird

.PHONY: stop-simulator
stop-simulator:
  docker stop simulator

.PHONY: stop-messagebird
stop-messagebird:
  docker stop messagebird

.PHONY: restart
restart:
  $(MAKE) stop
  $(MAKE) start