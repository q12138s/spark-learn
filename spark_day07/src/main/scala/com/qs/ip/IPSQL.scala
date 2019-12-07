package com.qs.ip

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object IPSQL {

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

    val ipAddr: DataFrame = spark.read.textFile("datas/ips/ip.txt")
      .filter(line => null != line && line.split("\\|").length == 15)
      .map(line => {
        val arr: Array[String] = line.trim.split("\\|")
        (arr(2).toLong, arr(3).toLong, arr(arr.length - 2), arr(arr.length - 1))
      }).toDF("startIp","endIp","longitude","latitude")


    val userIp: DataFrame = spark.read.textFile("datas/ips/20090121000132.394251.http.format")
      .filter(line => null != line && line.trim.split("\\|").length > 2)
      .map(line => {
        val ipString: String = line.trim.split("\\|")(1)
        val ipLong: Long = IPUtils.ipToLong(ipString)
        ipLong
      }).toDF("iplong")
    ipAddr.createOrReplaceTempView("ipAddr")
    userIp.createOrReplaceTempView("userIP")
    ipAddr.printSchema()
    userIp.printSchema()

    spark.sql(
      """
        |select b.longitude,b.latitude,count(1)
        |from userIp a join ipAddr b on a.iplong >= b.startIp and a.iplong <= b.endIp
        |group by b.longitude,b.latitude
      """.stripMargin).show(20,false)


    spark.stop()
  }

}
