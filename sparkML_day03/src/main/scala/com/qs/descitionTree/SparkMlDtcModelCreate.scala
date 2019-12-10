package com.qs.descitionTree

import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object SparkMlDtcModelCreate {
//  * 1-准备环境sparksession
//  * 2-准备数据读取
//  * 3-解析数据-对数据实现最简单的数据信息的查看schema
//  * 4-特征工程
//  * 5-准备算法----业务+数据----分类问题
//  * 6-训练模型
//  * 7-模型预测
//  * 8-模型校验
//  * 9-保存模型
//  * 10-加载保存的模型
  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("IrisSparkCoreLoader").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    import spark.implicits._

    val inputData: DataFrame = spark.read.format("libsvm")
      .load("C:\\Users\\admin\\IdeaProjects\\spark\\sparkML_day03\\datas\\sample_libsvm_data.txt")
    inputData.printSchema()

    val Array(train,testSet): Array[Dataset[Row]] = inputData.randomSplit(Array(0.8,0.2),seed=123L)
//4-特征工程
  val dtc: DecisionTreeClassifier = new DecisionTreeClassifier()
    .setFeaturesCol("features")
    .setLabelCol("label")
    .setImpurity("entropy")
    .setMaxDepth(5)
    .setPredictionCol("predicetion")

  //    * 6-训练模型
  val model: DecisionTreeClassificationModel = dtc.fit(train)
  //  * 7-模型预测
  val y_train_pred: DataFrame = model.transform(train)
  val y_test_pred: DataFrame = model.transform(testSet)

  //  * 8-模型校验
  val evaluator: MulticlassClassificationEvaluator = new MulticlassClassificationEvaluator()
    .setLabelCol("label")
    .setPredictionCol("predicetion")
    .setMetricName("accuracy")
  val y_test_accuracy_score: Double = evaluator.evaluate(y_train_pred)
  val y_train_accuracy_score: Double = evaluator.evaluate(y_train_pred)
  println(y_test_accuracy_score)
  println(y_train_accuracy_score)

  }

}
