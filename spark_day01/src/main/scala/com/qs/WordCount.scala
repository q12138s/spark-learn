package com.qs

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.Range.Inclusive

object WordCount {
  def main(args: Array[String]): Unit = {

    //创建SparkContext
    val conf = new SparkConf().setAppName("wc").setMaster("local[2]")
      .set("spark.testing.memory", "512000000")
//    conf.setAppName("wc")
//    conf.setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    //读取文件
    val fileRDD: RDD[String] = sc.textFile("G:\\ab.txt")
//    //处理数据
//    val result: Array[(String, Int)] = fileRDD.flatMap(_.split(" "))
//      .map((_, 1))
//      .reduceByKey(_ + _)
//      .collect()
//    result.foreach(println)
    fileRDD.flatMap(_.split(" "))
      .map((_,1))
      .reduceByKey(_+_)
      .map(tuple => (tuple._2,tuple._1))
      .sortByKey(false)
      .take(2)
      .foreach(println)


    val a1: Inclusive = 1 to 10
    val newRDD: RDD[Int] = sc.parallelize(a1,4)
    println(s"${newRDD.getNumPartitions}")

    Thread.sleep(100000000L)
    sc.stop()

  }
}
