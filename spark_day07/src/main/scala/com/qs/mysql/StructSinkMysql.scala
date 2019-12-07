package com.qs.mysql

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, SparkSession}

object StructSinkMysql {
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
    //todo 获取数据
    val inputData: DataFrame = spark.readStream
      .format("socket")
      .option("host", "hadoop01")
      .option("port", "9999")
      .load()
    //todo 处理数据
    val rsData: DataFrame = inputData
      .as[String]
      .filter(line => null != line && line.trim.split(" ").length > 0)
      .flatMap(_.split(" "))
      .groupBy($"value")
      .count()
  //todo 输出数据
    val query: StreamingQuery = rsData
      .writeStream
        .outputMode(OutputMode.Complete())
      .foreach(new MysqlSink())
      .start()
    //todo 启动
    query.awaitTermination()


  }
}
