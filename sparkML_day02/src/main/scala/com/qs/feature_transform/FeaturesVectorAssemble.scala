package com.qs.feature_transform

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession

object FeaturesVectorAssemble {

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

    val dataset = spark.createDataFrame(
      Seq((0, 18, 1.0, Vectors.dense(0.0, 10.0, 0.5), 1.0),
        (1, 20, 2.0, Vectors.dense(0.1, 11.0, 0.5), 0.0))
    ).toDF("id", "hour", "mobile", "userFeatures", "clicked")
    dataset.show(false)

    new VectorAssembler()
      .setInputCols(Array("hour", "mobile", "userFeatures", "clicked"))
      .setOutputCol("features")
      .transform(dataset)
      .show(false)
  }

}
