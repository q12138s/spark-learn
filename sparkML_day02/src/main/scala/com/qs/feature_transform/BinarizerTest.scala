package com.qs.feature_transform

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.{Binarizer, Bucketizer}
import org.apache.spark.sql.SparkSession

object BinarizerTest {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf()
      .setMaster(this.getClass.getSimpleName.stripSuffix("$"))
      .setMaster("local[2]")
      .set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession
      .builder()
      .config(conf)
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

//    val data = Array(-0.5, -0.3, 0.0, 0.2)
//    val df = spark.createDataFrame(data.map(Tuple1.apply)).toDF("features")
//    val bucketizer: Bucketizer = new Bucketizer()
//      .setInputCol("features")
//      .setOutputCol("bucketResult")
//      .setSplits(Array(Double.NegativeInfinity, -0.5, 0, 0.5, Double.PositiveInfinity))
//    bucketizer.transform(df).show(false)

    val data = Array((0, 0.1), (1, 0.8), (2, 0.2))
    val dataFrame = spark.createDataFrame(data).toDF("label", "feature")

    val binarizer: Binarizer = new Binarizer()
      .setInputCol("feature")
      .setOutputCol("binarized_feature")
      .setThreshold(0.5)

    val binarizedDataFrame = binarizer.transform(dataFrame)
    binarizedDataFrame.show(false)
//    val binarizedFeatures = binarizedDataFrame.select("binarized_feature")
//    binarizedFeatures.collect().foreach(println)
  }

}
