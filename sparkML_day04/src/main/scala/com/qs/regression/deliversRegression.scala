package com.qs.regression

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel, LinearRegressionTrainingSummary}
import org.apache.spark.sql.SparkSession
object deliversRegression {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    val data = spark.createDataFrame(Seq(
      (9.3, Vectors.dense(100, 4)),
      (4.8, Vectors.dense(50, 3)),
      (8.9, Vectors.dense(100, 4)),
      (6.5, Vectors.dense(100, 2)),
      (4.2, Vectors.dense(50, 2)),
      (6.2, Vectors.dense(80, 2)),
      (7.4, Vectors.dense(75, 3)),
      (6.0, Vectors.dense(65, 4)),
      (7.6, Vectors.dense(90, 3)),
      (6.1, Vectors.dense(90, 2))
    )).toDF("label", "features")
    //如果想用文件的导入方式就采用vectorAssemnble方法
    //2-回归问题的算法
    val lr: LinearRegression = new LinearRegression()
      .setFeaturesCol("features")
      .setLabelCol("label")
      .setMaxIter(20)
      .setStandardization(true)
    //内部实现了standscler
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
