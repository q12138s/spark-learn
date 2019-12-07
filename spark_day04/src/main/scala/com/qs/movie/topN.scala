package com.qs.movie

import java.util.Properties

import org.apache.spark.sql.{DataFrame, Dataset, Row, SaveMode, SparkSession}

object topN {
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

    val inputRdd: Dataset[String] = spark.read.textFile("spark_day04/datas/ml-1m/ratings.dat")
    //ETL
    val etlRdd: Dataset[(String, String, String, String)] = inputRdd
      .filter(line => null != line && line.trim.split("::").length == 4)
      .mapPartitions(part => {
        part.map(line => {
          val Array(userId, itemId, rate, time): Array[String] = line.trim.split("::")
          (userId, itemId, rate, time)
        })
      })
    val etlData: DataFrame = etlRdd.toDF("userId","itemId","rate","time")
    etlData.printSchema()
    etlData.show(10)

    //SQL
    etlData.createOrReplaceTempView("movie")
    val rs1: DataFrame = spark.sql(
      """
        | SELECT itemId,round(avg(rate),2) as avg_rate,count(1) as cnt FROM movie
        | GROUP BY itemId HAVING cnt > 2000 ORDER BY avg_rate DESC,cnt DESC LIMIT 10
    """.stripMargin
    )
    rs1.printSchema()
    rs1.show(10)

    //DSL
    val rs2: Dataset[Row] = etlData
      .select($"itemId", $"rate")
      .groupBy($"itemId")
      .agg(
        round(avg($"rate"), 2).as("avg_rate"),
        count($"itemId").as("cnt")
      )
      .filter($"cnt" > 2000)
      .orderBy($"avg_rate".desc, $"cnt".desc)
      .limit(10)
    rs2.printSchema()
    rs2.show(10)


    //输出到mysql
    rs2
        .coalesce(1)
        .write
        .mode(SaveMode.Append)
        .jdbc("jdbc:mysql://hadoop01:3306/test?user=root&password=mysql",
    "movie_rate_top10",
    new Properties())

    Thread.sleep(100000L)
    spark.stop()
  }
}
