package com.lineman.main

import com.lineman.utils.{HiveUtils, LoggingUtils, PostgresUtils, SparkUtils}
import org.apache.spark.sql.functions.{col, date_format, lit, round}

object Ingestion extends LoggingUtils{
  def main(args: Array[String]): Unit = {
    println("Hello Line Man Wongnai")
    logger.info("Hello Line Man Wongnai")

    val orderDf = PostgresUtils.readPostgresTable("order_detail")
      .withColumn(
        "discount",
        round(col("discount"), 2)
      ).withColumn(
        "dt",
        date_format(col("order_created_timestamp"), "yyyyMMdd")
      )

    val restaurantDf = PostgresUtils.readPostgresTable("restaurant_detail")
      .withColumn(
        "latitude",
        round(col("latitude"), 8)
      ).withColumn(
      "longitude",
      round(col("longitude"), 8)
    ).withColumn("dt", lit("latest"))

    orderDf.show(20, false)
    restaurantDf.show(20, false)

    logger.info(">>>> READ DATA FROM POSTGRESQL")

    HiveUtils.writeToHive(orderDf, "lineman", "order_detail", Array("dt"), "overwrite")
    HiveUtils.writeToHive(restaurantDf, "lineman", "restaurant_detail", Array("dt"), "overwrite")

    logger.info(">>>> WROTE DATA TO HIVE")

    val df1 = HiveUtils.readFromHive("lineman", "order_detail").withColumn("Source", lit("Hive"))
    val df2 = HiveUtils.readFromHive("lineman", "restaurant_detail").withColumn("Source", lit("Hive"))
    df1.show(20, false)
    df2.show(20, false)

    logger.info(">>>> READ DATA FROM HIVE")

    // Close SparkSession
    SparkUtils.closeSpark
  }
}
