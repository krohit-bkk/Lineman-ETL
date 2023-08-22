package com.lineman.utils

import org.apache.spark.sql.{DataFrame, Dataset, Row, SaveMode}

object PostgresUtils {
  // Define JDBC connection properties for PostgreSQL
  val jdbcUrl = "jdbc:postgresql://postgres-metastore:5432/lineman"
  val username = "hive"
  val password = "password"
  val connectionProperties = new java.util.Properties()
  connectionProperties.setProperty("user", this.username)
  connectionProperties.setProperty("password", this.password)
  connectionProperties.setProperty("driver", "org.postgresql.Driver")

  // Read data from PostgreSQL
  def readPostgresTable(tableName: String) = {
    // Read the PostgreSQL table into a DataFrame using JDBC
//    val postgresTableDF: DataFrame = SparkUtils.getSpark.read
//      .format("jdbc")
//      .option("url", this.jdbcUrl)
//      .option("dbtable", tableName)
//      .option("user", this.username)
//      .option("password", this.password)
//      .load()

    val postgresTableDF : DataFrame = SparkUtils.getSpark.read.jdbc(jdbcUrl, tableName, connectionProperties)

    postgresTableDF
  }

  // Write table to PostgeSQL
  def writePostgresTable(df: Dataset[Row], tableName: String, mode: String) = {
    val saveMode = if (mode.toLowerCase == "overwrite") SaveMode.Overwrite else SaveMode.Append

    df.write
      .format("jdbc")
      .option("url", jdbcUrl)
      .option("dbtable", tableName)
      .option("user", username)
      .option("password", password)
      .mode(saveMode)
      .save()
  }
}
