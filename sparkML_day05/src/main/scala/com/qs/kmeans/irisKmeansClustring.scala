package com.qs.kmeans

import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.feature.{MinMaxScaler, MinMaxScalerModel, VectorAssembler}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

object irisKmeansClustring {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]")
      .set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    //准备数据
    val data: DataFrame = spark.read.format("csv").option("header", "true")
      .option("inferschema", true).option("seq", ",")
      .load("sparkML_day05/datas/iris.csv")

    data.printSchema()
    //特征工程
    val vec: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("sepal_length", "sepal_width", "petal_length", "petal_width"))
      .setOutputCol("features")
    val vecRs: DataFrame = vec.transform(data)
    vecRs.show(5,false)
    val scaler: MinMaxScaler = new MinMaxScaler()
      .setInputCol("features")
      .setOutputCol("std_features")
    val scalerModel: MinMaxScalerModel = scaler.fit(vecRs)
    val scalerRs: DataFrame = scalerModel.transform(vecRs)

    //准备算法-------kmeans
    val means: KMeans = new KMeans()
      .setK(3)
      .setFeaturesCol("std_features")
      .setPredictionCol("prces")
      .setInitMode("k-means||")
    val model: KMeansModel = means.fit(scalerRs)
    model.transform(scalerRs).show(false)
    println(model.computeCost(scalerRs))
    model.clusterCenters.foreach(println(_))
  }

}
