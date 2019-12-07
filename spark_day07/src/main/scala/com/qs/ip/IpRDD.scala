package com.qs.ip

import org.apache.spark
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

object IpRDD {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("rdd")
      .setMaster("local[2]")
      .set("spark.testing.memory", "512000000")
    val sc = new SparkContext(conf)
    //设置日志级别
    sc.setLogLevel("WARN")

    val ipAddr: Array[(Long, Long, String, String)] = sc.textFile("datas/ips/ip.txt")
      .filter(line => line != null && line.trim.split("\\|").length == 15)
      .mapPartitions(part => {
        part.map(line => {
          val arr: Array[String] = line.trim.split("\\|")
          (arr(2).toLong, arr(3).toLong, arr(arr.length - 2), arr(arr.length - 1))
        })
      })
      .collect()
    val userIP: RDD[Int] = sc.textFile("datas/ips/20090121000132.394251.http.format")
      .filter(line => line != null && line.trim.split("\\|").length >2)
      .mapPartitions(part => {
        part.map(line => {
          val arr: Array[String] = line.split("\\|")
          val ipString: String = arr(1)
          val ipLong: Long = IPUtils.ipToLong(ipString)
          val index: Int = IPUtils.binarySezrch(ipLong, ipAddr)
          index
        }
        )
      })
    val rsData: RDD[((String, String), Int)] = userIP
      .filter(line => line != -1)
      .mapPartitions(part => {
        part.map(index => {
          val (_, _, longitude, latitude) = ipAddr(index)
          ((longitude, latitude), 1)
        })
      })
      .reduceByKey(_ + _)
      .sortBy(_._2, false)

//    rsData.coalesce(1).foreach(println)
      rsData.foreachPartition(part=>IPUtils.saveToMysql(part))

    sc.stop()
  }

}
