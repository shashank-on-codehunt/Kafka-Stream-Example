# Kafka-Stream-Example

# Download Kafka Binaries  
* make sure you have downloaded the binaries and setup then into you local folder 
[Apache Kafka Download Link](https://kafka.apache.org/downloads)

# Start Kafka Using KRaft
* Step1 : Generate Random UUID
```
./bin/kafka-storage.sh random-uuid
```
* Step 2 
```
./bin/kafka-storage.sh format -t {OuputOfStep1} --config ./config/kraft/server.properties
```

this will ouput where kafka will store the data 

```
metaPropertiesEnsemble=MetaPropertiesEnsemble(metadataLogDir=Optional.empty, dirs={/tmp/kraft-combined-logs: EMPTY})
Formatting /tmp/kraft-combined-logs with metadata.version 3.7-IV4.
```
* To Start Kafka Server 
```
./bin/kafka-server-start.sh ./config/kraft/server.properties

```
# Topics Creations 
### Create Input Topic 
* create topic with name   `expenses-input`
```warp-runnable-command
./bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 2 --topic expenses-input
```
### Create Output Topic 
* create topic with name  `expense-category-ouput` 
```warp-runnable-command
./bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 2 --topic expense-category-ouput

```
### List Down all the Topics 
```warp-runnable-command
./bin/kafka-topics.sh --list --bootstrap-server localhost:9092

```
## Producer Consumer Pair\. 
### Produce your Data 
```warp-runnable-command
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic expenses-input
```
### Simply Consume the Data 
```warp-runnable-command
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic expenses-input --from-beginning

```
# Use Kafka Streams 
***
1. Make sure the application jar is running on local using `java -jar <jarName>` or Run Directly the Application.

### Consume from Output Topic
```warp-runnable-command
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic expense-category-ouput \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer

```
# Sample Inputs 
```warp-runnable-command
Food 340
Travel 4500
Assets 7000
Food 1000
Travel 1000
Lease 50000
Food 1000
Travel 5000

```
