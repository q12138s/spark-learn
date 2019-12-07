package com.qs.eventTime

import java.sql.Timestamp

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable

object StructuredWindow {
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
    val inputData: DataFrame = spark.readStream
      .format("socket")
      .option("host", "hadoop01")
      .option("port", "9999")
      .load()
    //todo 处理数据
    val rsData = inputData
      .as[String]
      .filter(line => null != line && line.trim.length > 0)
      .flatMap{line=>
        // 将每行数据进行分割单词: 2019-10-12 09:00:02,cat dog
        val arr: mutable.ArrayOps[String] = line.split(",")
        //\\s表示 空格,回车,换行等空白符,
        arr(1).split("\\s+")
          .map(word=>{
            (Timestamp.valueOf(arr(0)),word)
          })
      }.toDF("timestamp","word")
      .groupBy(
        window($"timestamp","10 seconds","5 seconds"),
        $"word"
      )
      .count()
      .orderBy($"window")
  //todo 输出数据
    val query: StreamingQuery = rsData
      .writeStream
      .outputMode(OutputMode.Complete())
      .format("console")
      .option("numRows", "10")
      .option("truncate", "false")
      .start()
    //todo 启动
    query.awaitTermination()


  }
}
