package com.lineman.main

import com.lineman.utils.LoggingUtils
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types._


object Main extends LoggingUtils{
  def main(args: Array[String]): Unit = {
    // Create a SparkSession
    val spark: SparkSession = SparkSession.builder()
      .appName("DataFrameToHive")
      .enableHiveSupport()
      .getOrCreate()

    spark.conf.set("hive.metastore.uris", "thrift://hive-metastore:9083")


    // Create a sample DataFrame
    // Given data and columns
    import spark.implicits._
    val data = Seq(("John", 28), ("Alice", 25), ("Bob", 32))
    val columns = Seq("name", "age")
    val df : DataFrame= spark.createDataFrame(data).toDF(columns: _*)

    df.show()

    spark.sql("CREATE DATABASE IF NOT EXISTS xyz").show()

    // Save the DataFrame to a Hive table
    df.withColumn("Sheher", lit("BKK")).write
      .mode("append") // You can use "append", "overwrite", "ignore", or "error"
      .saveAsTable("xyz.my_table_name1")

    val df1 = spark.read.table("xyz.my_table_name1")
    df1.show()
    // Stop the SparkSession
    spark.stop()
  }
}
