package com.lineman.main

import com.lineman.utils.{HiveUtils, LoggingUtils, SparkUtils}
import org.apache.spark.sql.functions.{avg, coalesce, col, count, lit, round, when}

object Transformation extends LoggingUtils {
  def main(args: Array[String]): Unit = {
    println("Hello Line Man Wongnai")
    logger.info("Hello Line Man Wongnai")

    // Read the source table
    val _orderDetailDf = HiveUtils.readFromHive("lineman", "order_detail")
    val _restaurantDetailDf = HiveUtils.readFromHive("lineman", "restaurant_detail")

    // Transformations - Enrich restaurant_detail
    val restaurantDetailDf = _restaurantDetailDf.withColumn(
      "cooking_bin",
      when(
        col("estimated_cooking_time").between(10, 40), lit(1)
      ).when(
        col("estimated_cooking_time").between(41, 80), lit(2)
      ).when(
        col("estimated_cooking_time").between(81, 120), lit(3)
      ).otherwise(
        lit(4)
      )
    )

    // Transformations - Enrich order_detail
    val orderDetailDf = _orderDetailDf.withColumn(
      "discount_no_null",
      coalesce(col("discount"), lit(0))
    )

    // Write the processed __restaurant_detail_new__ and __order_detail_new__
    HiveUtils.writeToHive(restaurantDetailDf, "lineman", "__restaurant_detail_new__", Array("dt"), "append")
    HiveUtils.writeToHive(orderDetailDf, "lineman", "__order_detail_new__", Array("dt"), "append")

    // Further analysis
    val joinedDF = restaurantDetailDf.join(
      orderDetailDf,
      restaurantDetailDf("id") === orderDetailDf("restaurant_id"),
      "left"
    ).drop(
      "id"
    )

    // Get avg discount for each category
    val avgDiscountPerCategory = joinedDF
      .groupBy(col("category"))
      .agg(
        avg("discount").alias("avg_discount")
      ).withColumn(
      "avg_discount",
      round(col("avg_discount"), 2)
    )

    // Row count per each cooking_bin
    val rowCntPerCookingBin = joinedDF
      .groupBy("cooking_bin")
      .agg(
        count("*").alias("row_count")
      )

    // NOTE: Check results - comment out in production
    avgDiscountPerCategory.show(10, false)
    rowCntPerCookingBin.show(10, false)

    // Get samples to HDFS as CSV
    HiveUtils.copyCsvToHDFS(avgDiscountPerCategory, "/user/hive/warehouse/temp/avgDiscountPerCategory")
    HiveUtils.copyCsvToHDFS(rowCntPerCookingBin, "/user/hive/warehouse/temp/rowCntPerCookingBin")

  }
}
