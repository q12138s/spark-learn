package com.qs.regression

import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object SparkMLlibsvmDataRandomForestModel {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    val data: DataFrame = spark.read.format("libsvm").load("sparkML_day04/datas/sample_libsvm_data.txt")
    data.printSchema() //label+features
    val Array(trainingSet,testSet): Array[Dataset[Row]] = data.randomSplit(Array(0.8,0.2),seed = 123L)
    //    * 4-准备算法
    //      .setNumTrees()   默认值20----树的个数
    //      .setFeaturesCol() 特征列---需要使用数据整合好的
    //      .setPredictionCol()  预测列---用户自己定义的
    //      .setSubsamplingRate()  下采样---range (0, 1]
    //      .setFeatureSubsetStrategy()  特征采样，默认是auto，sqrt是分类，onethird是分类问题
    //      .setLabelCol()  标签列
    //      .setImpurity()  不纯度的度量---entropy和gini
    //      .setMaxDepth()
    val randomForestClassifier: RandomForestClassifier = new RandomForestClassifier()
      .setNumTrees(20)
      .setFeaturesCol("features")
      .setPredictionCol("prces")
      .setSubsamplingRate(0.8)
      .setFeatureSubsetStrategy("auto")
      .setLabelCol("label")
      .setImpurity("gini")
      .setMaxDepth(5)
      .setRawPredictionCol("raw_prces")
    //    * 5-模型训练
    val model: RandomForestClassificationModel = randomForestClassifier.fit(trainingSet)
    //    * 6-模型的预测
    val y_pred: DataFrame = model.transform(testSet)
    //  校验
    val evaluator: BinaryClassificationEvaluator = new BinaryClassificationEvaluator()
      .setMetricName("areaUnderROC")
      .setLabelCol("label")
      .setRawPredictionCol("raw_prces")
    val auc: Double = evaluator.evaluate(y_pred)
    println("AUC value is:",auc)

  }

}
