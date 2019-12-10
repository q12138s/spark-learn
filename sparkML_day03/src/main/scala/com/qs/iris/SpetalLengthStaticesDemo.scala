package com.qs.iris

import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object SpetalLengthStaticesDemo {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("IrisSparkCoreLoader").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    val inputRDD: RDD[String] = sc.textFile("C:\\Users\\admin\\IdeaProjects\\spark\\sparkML_day03\\datas\\iris.data")
    val data: RDD[linalg.Vector] = inputRDD
        .map(_.split(",")(0)).map(_.toDouble)
          .map(x => Vectors.dense(x))

    val summary: MultivariateStatisticalSummary = Statistics.colStats(data)

  }

}
