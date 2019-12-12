package com.qs.clustering

import org.apache.spark.ml.feature.{StringIndexer, StringIndexerModel, VectorAssembler}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

object HousePriceRegression {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]")
          .set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    val data: DataFrame = spark.read.format("csv")
      .option("header", "true")
      .option("sep", ";")
      .load("sparkML_day05/datas/houseprice.txt")
    data.printSchema()

    //特征工程
    val usedData: DataFrame = data.select(
      data("square").cast("double"),
      data("price").cast("double"),
      data("type")
    )

    val indexer: StringIndexer = new StringIndexer()
      .setInputCol("type")
      .setOutputCol("indexer_type")
    val indexerModel: StringIndexerModel = indexer.fit(usedData)
    val indexerRs: DataFrame = indexerModel.transform(usedData)
    indexerRs.show(false)
    val vec: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("square", "indexer_type"))
      .setOutputCol("feature")
    val vecRs: DataFrame = vec.transform(indexerRs)
    vecRs.show(false)
    //选择算法
    val regression: LinearRegression = new LinearRegression()
      .setElasticNetParam(0.3)
      .setSolver("normal")
      .setTol(1E-6)
      .setMaxIter(100)
      .setLabelCol("price")
      .setFeaturesCol("feature")
    //训练
    val model: LinearRegressionModel = regression.fit(vecRs)
    //模型的MAE等的模型评估量
    println(model.intercept)
    println(model.coefficients)
    println(model.summary.meanAbsoluteError)

  }

}
