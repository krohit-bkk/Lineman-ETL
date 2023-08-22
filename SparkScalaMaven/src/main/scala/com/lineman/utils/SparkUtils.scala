package com.lineman.utils

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkUtils extends LoggingUtils {
  // Create or get SparkSession object
  def getSpark : SparkSession = {
    val spark = SparkSession.builder()
      .appName("Lineman-ETL")
      .enableHiveSupport()
      .config("hive.exec.dynamic.partition.mode", "nonstrict")
      .getOrCreate()

    spark.conf.set("hive.metastore.uris", "thrift://hive-metastore:9083")
    spark.sparkContext.setLogLevel("WARN")

    spark
  }

  // Create database if not exists
  def checkAndCreateDatabase(databaseName: String) = {
    val spark = this.getSpark
    // Create the database if not exists
    spark.sql(s"CREATE DATABASE IF NOT EXISTS $databaseName")
    logger.info(s"Database $databaseName created successfully.")
  }

  // Close SparkSession object
  def closeSpark = getSpark.close()
}
