# Run:
### Run bot with docker-compose
```
docker-compose up
```
### Or run container with mysql
```
docker run --name mysql -p 3306:3306 -v /path/to/clojure_bot_db:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -d mysql
```
### Execute migrations
### Run bot
```
lein start
```
# Tests:
### Run tests
```
lein test
```