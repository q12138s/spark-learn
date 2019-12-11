package com.qs.regression

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel, LinearRegressionTrainingSummary}
import org.apache.spark.sql.SparkSession
object fatRegression {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    val data = spark.createDataFrame(Seq(
      (9.5, Vectors.dense(23)),
      (17.8, Vectors.dense(27)),
      (21.2, Vectors.dense(39)),
      (25.9, Vectors.dense(41)),
      (27.5, Vectors.dense(45)),
      (26.3, Vectors.dense(49)),
      (28.2, Vectors.dense(50)),
      (29.6, Vectors.dense(53)),
      (30.2, Vectors.dense(54)),
      (31.4, Vectors.dense(56)),
      (30.8, Vectors.dense(57)),
      (33.5, Vectors.dense(58)),
      (35.2, Vectors.dense(60)),
      (34.6, Vectors.dense(61))
    )).toDF("label", "features")
    //2-回归问题的算法
    val lr: LinearRegression = new LinearRegression()
      .setFeaturesCol("features")
      .setLabelCol("label")
    //3-训练模型
    val lrModel: LinearRegressionModel = lr.fit(data)
    //4-训练模型
    println("斜率：")
    println(lrModel.coefficients)
    println("截距：")
    println(lrModel.intercept)
    println("R2：")
    val summary: LinearRegressionTrainingSummary = lrModel.summary
    println(summary.r2)
    println(summary.meanAbsoluteError)
    println(summary.meanSquaredError)
    println(summary.rootMeanSquaredError)

  }

}
