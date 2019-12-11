package com.qs.iris

import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
object irisClaasifitionModel {

  def main(args: Array[String]): Unit = {
    //准备环境
    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    //准备数据
    //解析数据
    val data: DataFrame = spark.read.format("csv")
      .option("header", "true").option("inferschema", true)
      .load("sparkML_day04/datas/iris.csv")
    data.printSchema()

    //特征工程
    //标签列的处理
    val indexer: StringIndexer = new StringIndexer().setInputCol("class")
      .setOutputCol("classlabel")
    val model: StringIndexerModel = indexer.fit(data)
    val strRs: DataFrame = model.transform(data)
    //VectorAssembler
    val vec: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("sepal_length", "sepal_width", "petal_length", "petal_width"))
      .setOutputCol("features")
    val vecRs: DataFrame = vec.transform(strRs)
    val chi: ChiSqSelector = new ChiSqSelector()
      .setLabelCol("classlabel")
      .setFeaturesCol("features")
      .setNumTopFeatures(2)
      .setOutputCol("chiTest")
    val chiModel: ChiSqSelectorModel = chi.fit(vecRs)
    val chiRs: DataFrame = chiModel.transform(vecRs)
    //StandSclear
    val std: StandardScaler = new StandardScaler()
      .setInputCol("features")
      .setOutputCol("stand_features")
    val stdModel: StandardScalerModel = std.fit(chiRs)
    val stdResult: DataFrame = stdModel.transform(chiRs)
    stdResult.show(2, false)
    // 训练集和测试集的拆分
    val Array(trainSet, testSet): Array[Dataset[Row]] = stdResult.randomSplit(Array(0.8, 0.2), seed = 123L)
    trainSet.show()
    trainSet.printSchema()
    //    * 5-构建决策树模型
    val dtc: DecisionTreeClassifier = new DecisionTreeClassifier()
      .setLabelCol("classlabel")
      .setFeaturesCol("stand_features")
      .setPredictionCol("prces")
      .setImpurity("entropy")
    //    * 6-模型训练==算法+数据
    val dtcModel: DecisionTreeClassificationModel = dtc.fit(trainSet)
    //    * 7-模型预测
    val y_pred: DataFrame = dtcModel.transform(testSet)
    //    * 8-模型校验
    val evaluator: MulticlassClassificationEvaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("classlabel").setPredictionCol("prces").setMetricName("accuracy")
    val accuracy: Double = evaluator.evaluate(y_pred)
    println("accuracy is:", accuracy)
  }

}
