package com.qs.topN

import org.apache.spark.sql.{Dataset, SparkSession,DataFrame}
import scala.language.implicitConversions
object SparkSQLEdu {
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
    /**
     * 1.在所有的老师中求出最受欢迎的老师Top1
     * 求出现次数最多的老师
     * 2.求每个学科中最受欢迎老师的Top1
     * -》先按学科和老师来分组
     * 先求出每个学科的每个老师的次数
     * -》再求每个学科中，出现次数最多的老师
     * http://bigdata.itcast.cn/andy
     */
    val inputData: Dataset[String] = spark.read.textFile("spark_day06/src/datas/topN")
    val etlData: DataFrame = inputData
      .filter(line => null != line && line.trim.split("[/]").length > 0)
      .map(value => {
        val arr: Array[String] = value.split("[/]")
        val subject = arr(2).split("[.]")(0)
        val teacher = arr(3)
        (subject, teacher)
      }).toDF("subject", "teacher")
    //注册视图
    etlData.createOrReplaceTempView("subject")
    //缓存视图
    spark.catalog.cacheTable("subject")
    val teacherTopOne: DataFrame = spark.sql(
      """
        |select teacher,count(1) as number from subject group by teacher order by number desc limit 1
        |""".stripMargin)
    teacherTopOne.printSchema()
    teacherTopOne.show()
    //求每个学科中最受欢迎老师的Top1
    val rs2: DataFrame = spark.sql(
      """
        |select
        |subject,teacher,number
        | from
        | (select subject,teacher,number,row_number() over(partition by subject order by number desc) as r1 from
        | (select subject,teacher,count(1) as number from subject group by subject,teacher))
        | where r1 = 1
        |""".stripMargin)
    rs2.printSchema()
    rs2.show()

    spark.stop()
  }
}
