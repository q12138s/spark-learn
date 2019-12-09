package com.qs.feature_transform

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.QuantileDiscretizer
import org.apache.spark.sql.SparkSession

object QuantileTest {

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

    val data = Array((0, 18.0), (1, 19.0), (2, 8.0), (3, 5.0), (4, 2.2))
    var df = spark.createDataFrame(data).toDF("id", "hour")
//    df.show(false)
    df.describe().show(false)
    val discretizer: QuantileDiscretizer = new QuantileDiscretizer()
      .setInputCol("hour")
      .setOutputCol("selectResult")
      .setNumBuckets(3)
    discretizer.fit(df).transform(df).show(false)

  }

}
