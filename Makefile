.RECIPEPREFIX +=

smscsim:
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

.PHONY: run-smscsim
run-smscsim: smscsim
  docker run --rm --detach --network=host --env-file .env --name $< smscsim

.PHONY: run-message-bird
run-message-bird: message-bird
  docker run --rm --detach --network=host --env-file .env --name $< smscsim

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