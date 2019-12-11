package com.qs.iris

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{ChiSqSelector, StandardScaler, StandardScalerModel, StringIndexer, StringIndexerModel, VectorAssembler}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object irisClaasifitionModelVersion {

  def main(args: Array[String]): Unit = {

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
    // 训练集和测试集的拆分
    val Array(trainSet, testSet): Array[Dataset[Row]] = data.randomSplit(Array(0.8, 0.2), seed = 123L)

    //特征工程
    //标签列的处理
    val indexer: StringIndexer = new StringIndexer().setInputCol("class")
      .setOutputCol("classlabel")
//    val model: StringIndexerModel = indexer.fit(data)
//    val strRs: DataFrame = model.transform(data)
    //VectorAssembler
    val vec: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("sepal_length", "sepal_width", "petal_length", "petal_width"))
      .setOutputCol("features")
//    val vecRs: DataFrame = vec.transform(strRs)

    // 5-3 ChisquareTest
    val chi: ChiSqSelector = new ChiSqSelector()
      .setLabelCol("classlabel")
      .setFeaturesCol("features")
      .setNumTopFeatures(2)
      .setOutputCol("chiTest")
    //StandSclear
    val std: StandardScaler = new StandardScaler()
      .setInputCol("features")
      .setOutputCol("stand_features")
//    val stdModel: StandardScalerModel = std.fit(vecRs)
//    val stdResult: DataFrame = stdModel.transform(vecRs)
//    stdResult.show(2, false)
//    trainSet.show()
//    trainSet.printSchema()
    //    * 5-构建决策树模型
    val dtc: DecisionTreeClassifier = new DecisionTreeClassifier()
      .setLabelCol("classlabel")
      .setFeaturesCol("stand_features")
      .setPredictionCol("prces")
      .setImpurity("entropy")
    //    * 6-模型训练==算法+数据
    val pipeline: Pipeline = new Pipeline().setStages(Array(indexer,vec,chi,std,dtc))
    val model: PipelineModel = pipeline.fit(trainSet)

//    val dtcModel: DecisionTreeClassificationModel = dtc.fit(trainSet)
    //    * 7-模型预测
    val rs: DataFrame = model.transform(testSet)
//    val y_pred: DataFrame = dtcModel.transform(testSet)
    //    * 8-模型校验
    val evaluator: MulticlassClassificationEvaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("classlabel").setPredictionCol("prces").setMetricName("accuracy")
    val accuracy: Double = evaluator.evaluate(rs)
    println("accuracy is:", accuracy)
    model.stages(4).explainParams().foreach(print(_))
  }

}
