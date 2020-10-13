#!/bin/sh
export KAFKA_BIN=/home/jeremy/dev/kafka_2.12-2.5.0/bin
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic benchmarks
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic portfolioRequests
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic portfolioResults
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic portfolios
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic positions
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic rules
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic securities
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic tradeRequests
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --delete --topic tradeResults
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic benchmarks
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic portfolioRequests
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic portfolioResults
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic portfolios
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic positions
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic rules
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic securities
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic tradeRequests
$KAFKA_BIN/kafka-topics.sh --bootstrap-server uberkube06.slaq.org:9092 --create --partitions 24 --topic tradeResults
