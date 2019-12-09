package com.qs.feature_transform

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.sql.{DataFrame, SparkSession}

object StandScalerTest {

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

    val data: DataFrame = spark.read.format("libsvm").load("C:\\Users\\admin\\IdeaProjects\\spark\\sparkML_day02\\datas\\sample_libsvm_data.txt")
    data.show(false)

    new StandardScaler()
      .setInputCol("features")
      .setOutputCol("std")
      .setWithMean(true)
      .setWithStd(true)
      .fit(data)
      .transform(data)
      .show(false)

  }

}
