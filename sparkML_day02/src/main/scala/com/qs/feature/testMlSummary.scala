package com.qs.feature

import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.{Matrix, Vectors}
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object testMlSummary {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }
    sc.setLogLevel("WARN")
    val data: RDD[linalg.Vector] = sc.textFile("sparkML_day02/datas/testSummary.txt")
      .map(_.split("").map(_.toDouble))
      .map(x => Vectors.dense(x))
//    data.foreach(println)

    val data1: RDD[linalg.Vector] = sc.parallelize(Seq(
      Vectors.dense(1.0, 10.0, 100.0),
      Vectors.dense(2.0, 20.0, 200.0),
      Vectors.dense(3.0, 30.0, 300.0)
    ))
//    data1.foreach(println)
//
//    println("==============")
//    val summary: MultivariateStatisticalSummary = Statistics.colStats(data1)
//    println("non zeros:", summary.numNonzeros)
//    println("min value:", summary.min)
//    println("max value:", summary.max)
//    println("mean value:", summary.mean)
//    print("varience value:", summary.variance)


    val seriesX: RDD[Double] = sc.parallelize(Array(1, 2, 3, 3, 5))
    // a series// must have the same number of partitions and cardinality as seriesX
    val seriesY: RDD[Double] = sc.parallelize(Array(11, 22, 33, 33, 555))
    val corr: Double = Statistics.corr(seriesX,seriesY)
//    println(corr)

    println("=============")
    val corr1: Matrix = Statistics.corr(data1)
//    println(corr1)


  }

}
