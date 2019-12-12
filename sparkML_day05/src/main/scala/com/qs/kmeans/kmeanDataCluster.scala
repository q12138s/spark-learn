package com.qs.kmeans

import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object kmeanDataCluster {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]")
      .set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    //准备数据
    val data: RDD[String] = sc.textFile("sparkML_day05/datas/kmeans_data.txt")
    //解析数据
    val featureData: RDD[linalg.Vector] = data
      .map(x => Vectors.dense(x.split(" ").map(_.toDouble)))
    //准备工程
    //准备Kmeans算法
    val kmeansmodel: KMeansModel = KMeans.train(featureData,2,20,"k-means||")
    //训练模型
    val wssse: Double = kmeansmodel.computeCost(featureData)
    //模型预测
    println(wssse)
    kmeansmodel.clusterCenters.foreach(println(_))
  }

}
