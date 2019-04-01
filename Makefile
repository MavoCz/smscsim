.RECIPEPREFIX +=

.env:
  cp .env.example .env

.PHONY: test
test:
  mvn test

.PHONY: build
build: .env
  docker-compose run --rm -v "$$HOME/.m2":/root/.m2  maven mvn -Dmaven.test.skip=true package
  docker build -f Dockerfile -t smscsim:latest .
  docker image prune -f

.PHONY: run
run: .env
  docker-compose up -d smscsim

.PHONY: logs
logs:
  docker logs -f smscsim

.PHONY: start
start:
  $(MAKE) build
  $(MAKE) run

.PHONY: stop
stop:
  docker-compose down

.PHONY: restart
restart:
  $(MAKE) stop
  $(MAKE) start