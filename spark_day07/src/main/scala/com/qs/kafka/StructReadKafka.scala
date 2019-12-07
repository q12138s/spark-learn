package com.qs.kafka

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.types.{DoubleType, IntegerType}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object StructReadKafka {
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
    //todo 处理数据
    /**
     * 1-从Kafka中读取数据
     * {"orderId":"ec844809-34cf-47f3-ae42-ea2b7dfc9cfe","provinceId":33,"orderPrice":49.5}
     * 2-过滤：省份id小于等于10，并且订单价格大于等于50
     * 3-写入Kafka
     */
    val etl: Dataset[Row] = inputData
        .selectExpr("CAST(value AS STRING)")
      .as[String]
      .filter(line => null != line && line.trim.split(" ").length > 0)
      .select(
          get_json_object($"value","$.orderId").as("orderId"),
          get_json_object($"value","$.provinceId").cast(IntegerType).as("provinceId"),
          get_json_object($"value","$.orderPrice").cast(DoubleType).as("orderPrice")
      )
      .filter($"provinceId".lt(10).and($"orderPrice".geq(50)))
    val rsData: DataFrame = etl
      .select($"orderId".as("key"),
        to_json(struct($"provinceId", $"orderPrice")).as("value")
      )
  //todo 输出数据
    val query: StreamingQuery = rsData
      .writeStream
        .outputMode(OutputMode.Append())
      .format("kafka")
      .option("kafka.bootstrap.servers", "hadoop01:9092,hadoop02:9092,hadoop03:9092")
      .option("topic", "test09")
      .option("checkpointLocation","datas/spark/struct/checkpoint")
      .start()
    //todo 启动
    query.awaitTermination()


  }
}
