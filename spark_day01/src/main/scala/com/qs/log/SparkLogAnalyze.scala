package com.qs.log

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

object SparkLogAnalyze {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }

    val inputRdd: RDD[String] = sc.textFile("spark_day01/datas/logs/access.log")

    val etlRdd: RDD[(String, String, String)] = inputRdd
      .filter(line => null != line && line.trim.split(" ").length > 11)
      .map(line => {
        val arr: Array[String] = line.split(" ")
        (arr(0), arr(6), arr(10))
      })

    //缓存
    etlRdd.persist(StorageLevel.MEMORY_AND_DISK_2)
    //pv
    val pv: Long = etlRdd
      .map(tuple => tuple._2)
      .filter(line => line != null && line.trim.length > 0)
      .count()

    //uv
    val uv: Long = etlRdd
      .map(tuple => tuple._1)
      .distinct()
      .count()
    //来源top20
    val top20: Array[(String, Int)] = etlRdd
      .map(tuple => tuple._3)
      .map(value => (value, 1))
      .reduceByKey(_ + _)
      .sortBy(_._2,false)
      .take(20)

    etlRdd.unpersist(true)

    println(s"pv=${pv}\tuv=${uv}")

    top20.foreach(println)


    Thread.sleep(10000000L)
    sc.stop()

  }

}
