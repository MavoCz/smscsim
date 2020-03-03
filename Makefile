.RECIPEPREFIX +=

simulator:
  cp .env.$@ .env

message-bird:
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
  docker run --rm --detach --network=host --env-file .env --name $< smscsim

.PHONY: run-message-bird
run-message-bird: message-bird
  docker run --rm --detach --network=host --env-file .env --name $< smscsim

.PHONY: logs
logs:
  docker logs -f simulator

.PHONY: start
start:
  $(MAKE) build
  $(MAKE) run-simulator
  $(MAKE) run-message-bird

.PHONY: start-simulator
start-simulator: build
 $(MAKE) run-simulator

.PHONY: start-message-bird
start-message-bird: build
 $(MAKE) run-message-bird

.PHONY: stop
stop:
  docker stop simulator
  docker stop message-bird

.PHONY: restart
restart:
  $(MAKE) stop
  $(MAKE) start