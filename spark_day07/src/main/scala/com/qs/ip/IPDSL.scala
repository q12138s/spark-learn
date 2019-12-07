package com.qs.ip

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.{DataFrame, SparkSession}

object IPDSL {

  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession
      .builder()
      .appName("SQL")
      .master("local[2]")
      .config("spark.testing.memory", "512000000")
      .config("spark.sql.shuffle.partitions", "2")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._
    import org.apache.spark.sql.functions._

    val ipAddr: Array[(Long, Long, String, String)] = spark.read.textFile("datas/ips/ip.txt")
      .filter(line => null != line && line.split("\\|").length == 15)
      .map(line => {
        val arr: Array[String] = line.trim.split("\\|")
        (arr(2).toLong, arr(3).toLong, arr(arr.length - 2), arr(arr.length - 1))
      }).collect()


    val userIp: DataFrame = spark.read.textFile("datas/ips/20090121000132.394251.http.format")
      .filter(line => null != line && line.trim.split("\\|").length > 2)
      .map(line => {
        val ipString: String = line.trim.split("\\|")(1)
//        val ipLong: Long = IPUtils.ipToLong(ipString)
        ipString
      }).toDF("ipString")


    val ipToLong: UserDefinedFunction = udf(IPUtils.ipToLong _)
    val getLocation: UserDefinedFunction = udf((ipLong: Long) => {
      val index: Int = IPUtils.binarySezrch(ipLong, ipAddr)
      val (_,_,longitude,latitude)=ipAddr(index)
      (longitude,latitude)
    })

    val rsData: DataFrame = userIp
      .select(
        getLocation(ipToLong($"ipString")).as("location")
      )
      .groupBy($"location").count()
    rsData.printSchema()

    rsData
        .select(
          ($"location._1").as("longitude"),
          ($"location._2").as("latitude"),
          ($"count").as("number")
        ).show()

    spark.stop()
  }

}
