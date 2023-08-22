package com.lineman.utils

import org.apache.spark
import org.apache.spark.sql.functions.{coalesce, col, lit}
import org.apache.spark.sql.jdbc.{JdbcDialect, JdbcDialects}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SaveMode, SparkSession}

// Hotornworks and Cloudera are stopping the support to
// connect to hive from Spark through the Thrift Server.
// https://stackoverflow.com/questions/48087779/sqlcontext-hivedriver-error-on-sqlexception-method-not-supported/48712929#48712929
private case object HiveDialect extends JdbcDialect {
  override def canHandle(url: String): Boolean = url.startsWith("jdbc:hive2")
  override def quoteIdentifier(colName: String): String = {
    colName.split('.').map(part => s"`$part`").mkString(".")
  }
}
object HiveUtils {
  // Hive connection details
  val jdbcUrl = s"jdbc:hive2://localhost:10000/"
  val username = "hive"
  val password = "password"

  // NOT NEEDED: Get SparkSession object and add hive-metastore-uri
  def getSpark : SparkSession = {
    val spark = SparkUtils.getSpark
    // Hive metastore uri for integration
    spark.conf.set("hive.metastore.uris", "thrift://hive-metastore:9083")

    spark
  }

  // Read tables from Hive
  def readFromHive(databaseName: String, tableName: String) = {
    // Register the dialect
    JdbcDialects.registerDialect(HiveDialect)
    val hiveTableDF : DataFrame = this.getSpark.read.table(s"${databaseName}.${tableName}")

    // Return dataframe
    hiveTableDF
  }

  // Write table to Hive
  def writeToHive(df: Dataset[Row], databaseName: String, tableName: String, partitionColumns: Array[String], mode: String): Unit = {
    val saveMode = if (mode.toLowerCase() == "overwrite") SaveMode.Overwrite else SaveMode.Append
    val _databaseName = if(databaseName == null) "default" else databaseName

    // Check if the database exists
    SparkUtils.checkAndCreateDatabase(_databaseName)

    // External path
    val extPath = s"file:///user/hive/warehouse/${_databaseName}.db/${tableName}/"

    // Write DataFrame to Hive table
    if (partitionColumns == null) {
      df.write
        .format("hive")
        .mode(saveMode)
        .option("path", extPath)
        .saveAsTable(s"${_databaseName}.${tableName}")
    }
    else {
      df.write
        .format("hive")
        .mode(saveMode)
        .partitionBy(partitionColumns: _*)
        .option("path", extPath)
        .saveAsTable(s"${_databaseName}.${tableName}")
    }
  }

  // Export dataframe as CSV
  def copyCsvToHDFS(df: DataFrame, path: String) = {
    df.coalesce(1)
      .write
      .format("csv")
      .option("header", "true")
      .option("delimiter", ",")
      .mode(SaveMode.Overwrite)
      .save(path)
  }
}
