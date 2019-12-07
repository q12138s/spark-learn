package com.qs.topN

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

object SparkCoreEdu {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }
    val inputRdd: RDD[String] = sc.textFile("spark_day06/src/datas/topN")
    val etlRdd: RDD[(String, String)] = inputRdd
      .filter(line => null != line && line.trim.split("[/]").length > 0)
      .map(value => {
        val arr: Array[String] = value.trim.split("[/]")
        val subject: String = arr(2).split("[.]")(0)
        val teacher: String = arr(3)
        (subject,teacher)
      })
    etlRdd.persist(StorageLevel.MEMORY_AND_DISK_SER)
    /**
     * 1.在所有的老师中求出最受欢迎的老师Top1
     * 求出现次数最多的老师
     * 2.求每个学科中最受欢迎老师的Top1
     * -》先按学科和老师来分组
     * 先求出每个学科的每个老师的次数
     * -》再求每个学科中，出现次数最多的老师
     * http://bigdata.itcast.cn/andy
     */
    val rs1: Array[(String, Int)] = etlRdd
      .map(tuple => (tuple._2, 1))
      .reduceByKey(_ + _)
      .sortBy(_._2,false)
      .take(1)
    rs1.foreach(println)
    println("==================================")

    val subjects: Array[String] = etlRdd
      //返回所有的学科
      .map(tuple => tuple._1)
      .distinct()
      .collect()
    val rs2: RDD[((String, String), Int)] = etlRdd
      .map(tuple => (tuple, 1))
      .reduceByKey(new SubjectPartition(subjects), _ + _)
      .mapPartitions(part => {
        part.toList.sortBy(_._2).take(1).iterator
      })
    rs2.foreach(println)



    sc.stop()

  }

}
