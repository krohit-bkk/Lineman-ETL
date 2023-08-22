#!/bin/bash

# Env setup
mkdir -p /root/logs
export PATH=/opt/hive/tools/spark/bin:/opt/hive/bin:/opt/hadoop/bin:/usr/local/openjdk-8/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
echo "$PATH" > /root/logs/path_log.log
echo `whoami` >> /root/logs/path_log.log

# Spark submits
spark-submit --jars /root/postgresql-42.5.1.jar --class com.lineman.main.Ingestion /root/SparkScalaMaven-1.0-SNAPSHOT.jar >> /root/logs/$(date +"%Y%m%d%H%M%S")_ingestion.log 2>&1
spark-submit --jars /root/postgresql-42.5.1.jar --class com.lineman.main.Transformation /root/SparkScalaMaven-1.0-SNAPSHOT.jar >> /root/logs/$(date +"%Y%m%d%H%M%S")_transformation.log 2>&1
