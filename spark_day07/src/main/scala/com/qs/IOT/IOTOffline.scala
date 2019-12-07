package com.qs.IOT

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object IOTOffline {

  def main(args: Array[String]): Unit = {


    val spark: SparkSession = SparkSession
      .builder()
      .appName(this.getClass.getSimpleName.stripSuffix("$"))
      .master("local[2]")
      .config("spark.testing.memory", "512000000")
      .getOrCreate()
    // 导入隐式转换，开发过程中，会经常出现RDD，dataFrame以及DataSet三者之间的转换
        import spark.implicits._
    //导入SQL的函数包
        import org.apache.spark.sql.functions._
    //设置日志级别
    spark.sparkContext.setLogLevel("WARN")

    val inputData: DataFrame = spark.read.json("datas/device/device.json")
    /**
     * 使用离线和实时两种方式统计:
     * 1）、信号强度大于10的设备
     * 2）、各种设备类型的数量
     * 3）、各种设备类型的平均信号强度
     * {"device":"redmi",
     * "deviceType": "phone",
     * "signal": 10,
     * "time": "2018-01-02 15:20:00"}
     */
    val rsData: DataFrame = inputData
      .filter($"signal".gt(10))
      .groupBy($"deviceType")
      .agg(
        count($"deviceType"),
        avg($"signal")
      )
    rsData.printSchema()
    rsData.show(10,false)

    spark.stop()
  }

}
