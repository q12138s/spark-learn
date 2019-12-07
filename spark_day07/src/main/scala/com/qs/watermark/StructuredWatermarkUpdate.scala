package com.qs.watermark

import java.sql.Timestamp

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}
import org.apache.spark.sql.{DataFrame, SparkSession}

object StructuredWatermarkUpdate {

  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession
      .builder()
      .appName("SQL")
      .master("local[2]")
      .config("spark.testing.memory", "512000000")
      .config("spark.sql.shuffle.partitions", "2")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._
    import org.apache.spark.sql.functions._
    val inputStream: DataFrame = spark.readStream
      .format("socket")
      .option("host", "hadoop01")
      .option("port", 9999)
      .load()
    val rsData: DataFrame = inputStream
      .as[String]
      .filter(line => null != line && line.trim.length > 0)
      .map(line => {
        val arr: Array[String] = line.trim.split(",")
        (arr(0), Timestamp.valueOf(arr(1)))
      })
      .toDF("word", "time")
      .withWatermark("time", "10 seconds")
      .groupBy(
        window($"time", "5 seconds", "5 seconds"),
        $"word"
      )
      .count()
    val query: StreamingQuery = rsData
      .writeStream
      .outputMode(OutputMode.Complete())
      .format("console")
      .option("numRows", "10")
      .option("truncate", "false")
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .start()
    query.awaitTermination()
  }

}
