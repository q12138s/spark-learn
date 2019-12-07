package com.qs.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{DoubleType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object RDD2DFOrDS {
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

    //获取RDD数据源
    val inputRdd: RDD[String] = spark.sparkContext.textFile("spark_day04/datas/ml-100k/u.data")
    //使用反射将当前RDD转换成拥有Schema的RDD
//    val tempRdd: RDD[MovieRate] = inputRdd
    //      .filter(line => null != line && line.trim.split("\t").length == 4)
    //      .map(line => {
    //        val Array(userId, itemId, rate, time) = line.trim.split("\t")
    //        MovieRate(userId, itemId, rate.toDouble, time.toLong)
    //      })
    //    val df1: DataFrame = tempRdd.toDF()
    //    df1.printSchema()
    //    df1.show(5)
    //    println("++++++++++++++++++++++++++++")
    //    val ds1: Dataset[MovieRate] = tempRdd.toDS()
    //    ds1.printSchema()
    //    ds1.show(5)


    //构造Schema
    val schema: StructType = new StructType(Array(
      StructField("userId", StringType, false),
      StructField("itemId", StringType, false),
      StructField("rate", DoubleType, false),
      StructField("time", LongType, false)
    ))
    //将RDD转换为Row类型
    val RowRDD: RDD[Row] = inputRdd
      .filter(line => null != line && line.trim.split("\t").length == 4)
      .map(line => {
        //开箱操作
        val Array(userId, itemId, rate, time) = line.trim.split("\t")
        //返回Row类型
        Row(userId, itemId, rate.toDouble, time.toLong)
      })
    //将一个Row类型的RDD和Schema合并得到一个DF
    val df2: DataFrame = spark.createDataFrame(RowRDD,schema)
    df2.printSchema()
    df2.show(5)

    //将DF默认类型Row更换成一个其他泛型就是DS
    val ds2: Dataset[MovieRate] = df2.as[MovieRate]
    ds2.printSchema()
    ds2.show(5)






  }
}
case class MovieRate(userId:String,itemId:String,rate:Double,time:Long) {

}
