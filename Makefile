.RECIPEPREFIX +=

DOCKER_IMAGE=smscsim

smsc-sim-bind1:
  cp .env.$@ .env

smsc-sim-bind2:
  cp .env.$@ .env

.PHONY: test
test:
  mvn test

.PHONY: build
build:
  docker-compose run --rm -v "$$HOME/.m2":/root/.m2  maven mvn -Dmaven.test.skip=true package
  docker build -f Dockerfile -t smscsim:latest .
  docker image prune -f

.PHONY: run-smsc-sim-bind1
run-smsc-sim-bind1: smsc-sim-bind1
  docker run --rm --detach --network=host --env-file .env --name $< $(DOCKER_IMAGE)

.PHONY: run-smscsim-sim-bind2
run-smsc-sim-bind2: smsc-sim-bind2
  docker run --rm --detach --network=host --env-file .env --name $< $(DOCKER_IMAGE)

.PHONY: log-smsc-sim-bind1
log-smsc-sim-bind1:
  docker logs -f smsc-sim-bind1

.PHONY: log-smsc-sim-bind2
log-smsc-sim-bind2:
  docker logs -f smsc-sim-bind2

.PHONY: start
start:
  $(MAKE) build
  $(MAKE) run-smsc-sim-bind1
  $(MAKE) run-smsc-sim-bind2

.PHONY: start-smsc-sim-bind1
start-smsc-sim-bind1: build
 $(MAKE) run-smsc-sim-bind1

.PHONY: start-smsc-sim-bind2
start-smsc-sim-bind2: build
 $(MAKE) run-smsc-sim-bind2

.PHONY: stop
stop:
  $(MAKE) stop-smsc-sim-bind1
  $(MAKE) stop-smsc-sim-bind2

.PHONY: stop-smsc-sim-bind1
stop-smsc-sim-bind1:
  docker stop smsc-sim-bind1

.PHONY: stop-smsc-sim-bind2
stop-smsc-sim-bind2:
  docker stop smsc-sim-bind2

.PHONY: restart
restart:
  $(MAKE) stop
  $(MAKE) start
