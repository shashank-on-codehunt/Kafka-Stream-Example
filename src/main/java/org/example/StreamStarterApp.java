package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Properties;

public class StreamStarterApp {
    public static void main(String[] args) {
        Properties config = new Properties();
        // Kafka Streams application will use this id to isolate its consumer group and internal topics from others
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "expense-streams-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        //determines what the Kafka consumer should do when there is no initial offset in Kafka
        // or if the current offset does not exist any more on the server (for example,
        // because that data has been deleted)
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // configuration is default and will be used unless a specific SerDe is specified for a particular operation.
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());



        //creates an instance of StreamsBuilder, which is a high-level Streams DSL provided by Kafka Stream
        // used to construct a Topology, which is an abstraction of a stream processing application
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, String> expenseCountInput = streamsBuilder.stream("expenses-input");



        // KTable is an abstraction of a changelog stream from a Kafka topic
        // Each data record in the changelog stream is considered an update (upsert in database terms)
        // because any previous value with the same key is overwritten.
        KTable<String, String> expenseCategoryTable = expenseCountInput
            .map((key, value) -> { // split the value by space and create a new key-value pair
                String[] parts = value.split(" ");
                return KeyValue.pair(parts[0], parts[1]);
            })
            .groupByKey() //groups the records by their key.
            // This is a prerequisite for the following aggregation operation (like reduce)
            .reduce((aggValue, newValue) -> // sum the costs
                    String.valueOf(Long.sum(Long.parseLong(aggValue), Long.parseLong(newValue))),
                // utility class provided by Kafka Streams to help with state store configuration.
                // uses an on-disk state store (RocksDB) for storing the stat
                // If the types of the keys and values in the state store are different from the default Serdes,
                // you will get a ClassCastException at runtime
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("items-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(Serdes.String()));


        // converts the KTable to a KStream
        expenseCategoryTable.toStream()
            //allows us to perform an action on each record in the stream without changing the stream itself.
            .peek((key, value) -> System.out.println("Key: " + key + ", Value: " + value))
            // line sends the records in the stream to the "expense-category-ouput" topic
            .to("expense-category-ouput", Produced.with(Serdes.String(), Serdes.String()));


        // creates a new Kafka Streams client instance.
        // it is a thread-safe, lightweight, and non-blocking Java client
        // for processing records from a Kafka cluster
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), config);
        // start the Kafka Streams application
        kafkaStreams.start();
    }
}
