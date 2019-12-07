package com.qs

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkRddRepartition {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }



    val inputRdd: RDD[String] = sc.textFile("spark_day01/datas/wordcount/input/wordcount.data")
    println(s"${inputRdd.getNumPartitions}")

    val dataRdd: RDD[String] = inputRdd.repartition(4)
    println(s"${dataRdd.getNumPartitions}")


    val result: RDD[(String, Int)] = dataRdd
      .filter(line => null != line && line.trim.split(" ").length > 0)
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
    println(s"${result.getNumPartitions}")

    val rs: RDD[(String, Int)] = result.coalesce(1)
    println(s"${rs.getNumPartitions}")

    rs.saveAsTextFile("spark_day01/datas/wordcount/output"+System.currentTimeMillis())
    sc.stop()

  }

}
