package com.qs.feature_transform

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.ChiSqSelector
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}

object ChiSquareTest {

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
    import spark.implicits._
    val data = Seq(
      (7, Vectors.dense(0.0, 0.0, 18.0, 1.0), 1.0),
      (8, Vectors.dense(0.0, 1.0, 12.0, 0.0), 0.0),
      (9, Vectors.dense(1.0, 0.0, 15.0, 0.1), 0.0)
    )
    val df: DataFrame = spark.createDataFrame(data).toDF("id","features","clicked")
    new ChiSqSelector()
      .setLabelCol("clicked")
      .setFeaturesCol("features")
      .setNumTopFeatures(2)
      .fit(df)
      .transform(df)
      .show(false)
  }

}
