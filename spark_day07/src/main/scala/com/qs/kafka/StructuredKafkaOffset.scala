package com.qs.kafka

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, SparkSession}

object StructuredKafkaOffset {

  def main(args: Array[String]): Unit = {

    val spark:SparkSession = SparkSession
      //构建
      .builder()
      //设置master
      .master("local[2]")
      //设置程序名称：工作中自己定义，具有标示性，方便监控
      .appName(this.getClass.getSimpleName.stripSuffix("$"))
      //配置，设置shuffle的分区数
      .config("spark.sql.shuffle.partitions","2")
      .config("spark.testing.memory", "512000000")
      .getOrCreate()
    //调整日志级别
    spark.sparkContext.setLogLevel("WARN")
    //导包
    import spark.implicits._
    import org.apache.spark.sql.functions._

    //todo 获取数据
    val inputData: DataFrame = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "hadoop01:9092,hadoop02:9092,hadoop03:9092")
      .option("subscribe", "test")
      .load()
    inputData.printSchema()

    inputData
      .select(
          $"topic",$"partition",$"offset"
      )
      .groupBy($"topic",$"partition")
      .agg(
        min($"offset").as("fromOffset"),
        max($"offset").as("maxOffset"),
        (max($"offset")+1).as("untilOffset")
      )


    // TODO: 针对流式应用来说，输出的是流
    val query: StreamingQuery = inputData.writeStream
      // TODO: 对流式应用输出来说，设置输出模式
      .outputMode(OutputMode.Complete())
      .format("console")
      .option("numRows", "10")
      .option("truncate", "false")
      // 流式应用，需要启动start
      .start()
    // 查询器等待流式应用终止
    query.awaitTermination()
  }

}
