package com.qs.json

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}

object StructSourceJson {
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
    val fields: Array[StructField] =
      Array(StructField("name",StringType,false),StructField("age",IntegerType,false),StructField("hobby",StringType,false))
    val schema:StructType = new StructType(fields)
    val inputData: DataFrame = spark.readStream
        .schema(schema)
        .json("datas/hobby/")
    //todo 处理数据
    //统计25以下的人的兴趣排名
    val rsData = inputData
//      .as[String]
//      .filter(line => null != line && line.trim.split(" ").length > 0)
      .filter($"age".lt(25))
      .groupBy($"hobby")
      .count()
      .orderBy($"count".desc)
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
