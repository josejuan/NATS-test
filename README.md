# how to run

Start NATS server:

```shell script
$ docker run -d --name nats-main -p 4222:4222 -p 6222:6222 -p 8222:8222 nats
...
```

Start pub/subs:

```shell script
$ for i in `seq 1 50`; do java -jar target/natsand-1.0-SNAPSHOT-jar-with-dependencies.jar > ~/tmp/nats-$i.log & done
```

Watch evolution:
```shell script
$ ls ~/tmp/nats-*.log | while read i; do tail -n1 $i; done
2670, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2496, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2283, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2459, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2632, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2618, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2571, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2487, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2707, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2538, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2621, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
2693, 9905, 10027, 10006, 10050, 10080, 10024, 9996, 10055, 10098
...
```
Where first column are the current last truncated 20 seconds (and the specific time to write output matter), the
second columns are the window from last 20 to 40 seconds, third from 40 to 20 and so son.

If you start a new pub/sub, the first window will be desynchronized (he lost events for that window):
```shell script
]$ java -jar target/natsand-1.0-SNAPSHOT-jar-with-dependencies.jar 
521
...
2059
424, 2146
...
10148, 2146
420, 10240, 2146
...
10109, 10240, 2146
451, 10192, 10240, 2146
...
2975, 10192, 10240, 2146
...
```
When others contains:
```shell script
3130, 10192, 10240, 10122, 9977, 10173, 10137, 10048, ...
```

# how to compile

```shell script
$ mvn clean compile assembly:single
...
```