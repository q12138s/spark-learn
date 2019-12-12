package com.qs.kmeans



import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object kmeanDataMLCluster {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]")
      .set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    //准备数据
    val data: DataFrame = spark.read.format("libsvm").load("sparkML_day05/datas/sample_kmeans_data.txt")
    data.printSchema()
    //准备工程
    //准备Kmeans算法
    val means: KMeans = new KMeans()
      .setInitMode("k-means||")
      .setK(2)
      .setPredictionCol("prces")
      .setFeaturesCol("features")
      .setMaxIter(100)
      .setTol(0.001)
    //训练模型
    val model: KMeansModel = means.fit(data)
    model.transform(data).show(false)
    //模型预测
    println(model.computeCost(data))

  }

}
