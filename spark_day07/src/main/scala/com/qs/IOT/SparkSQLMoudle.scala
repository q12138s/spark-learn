package com.qs.IOT

import org.apache.spark.sql.SparkSession

object SparkSQLMoudle {
  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession
      .builder()
      .appName(this.getClass.getSimpleName.stripSuffix("$"))
      .master("local[2]")
      .config("spark.testing.memory", "512000000")
      .getOrCreate()
    // 导入隐式转换，开发过程中，会经常出现RDD，dataFrame以及DataSet三者之间的转换
//    import spark.implicits._
    //导入SQL的函数包
//    import org.apache.spark.sql.functions._
    //设置日志级别
    spark.sparkContext.setLogLevel("WARN")





    spark.stop()
  }
}
