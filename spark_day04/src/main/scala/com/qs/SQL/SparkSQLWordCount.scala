package com.qs.SQL

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SparkSQLWordCount {
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


    val inputRDD: Dataset[String] = spark.read.textFile("spark_day04/datas/wordcount/input/wordcount.data")
//    inputRDD.printSchema()
    /**
     * *  第一个参数代表条数
     * *  第二个参数代表是否省略过长的值，默认只显示20个字符，
     * 如果该值超过20个，会进行省略显示
     */
//    inputRDD.show(20,false)
    /**
     * DSL
     */
    val DSLRdd: DataFrame = inputRDD
      .filter(line => null != line && line.trim.length > 0)
      .flatMap(_.split(" "))
      .groupBy("value")
      .count()
    DSLRdd.printSchema()
    DSLRdd.show(20,false)

    /**
     * SQL
     */
    val tempData: Dataset[String] = inputRDD
      .filter(line => null != line && line.trim.length > 0)
      .flatMap(_.split(" "))
    //注册成一个视图
    tempData.createOrReplaceTempView("wc")
    //SQL
    val SQLRdd: DataFrame = spark.sql(
      """
        |select value as word,count(1) as number
        | from
        |wc
        | group by
        |value
        | order by
        |number
        |desc
        |""".stripMargin
    )
    SQLRdd.printSchema()
    SQLRdd.show(20,false)

    spark.stop()
  }
}
