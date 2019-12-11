package com.qs.love

import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object LovaDataModelMlCreate {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    val inputData: DataFrame = spark.read.format("csv")
      .option("header", "true")
      .option("inferschema", "true")
      .load("sparkML_day04/datas/lovedata.csv")
    inputData.printSchema()

    //解析数据
    val vec: VectorAssembler = new VectorAssembler().setInputCols(Array("age", "handsome", "income", "is_gongwuyuan"))
      .setOutputCol("feature")
    val vecRs: DataFrame = vec.transform(inputData)
    //划分训练集测试集
    val Array(trainingSet,testSet): Array[Dataset[Row]] = vecRs.randomSplit(Array(0.8,0.2))
      //引入决策树算法
    val dtc: DecisionTreeClassifier = new DecisionTreeClassifier()
      .setLabelCol("is_date")
      .setFeaturesCol("feature")
      .setPredictionCol("pred")
      .setImpurity("entropy")
      .setRawPredictionCol("raw_prces")
    //构建决策树模型
    val dtcModel: DecisionTreeClassificationModel = dtc.fit(trainingSet)
    //模型预测
    val y_pred: DataFrame = dtcModel.transform(testSet)
    //校验
    y_pred.show(false)
    val evaluator: BinaryClassificationEvaluator = new BinaryClassificationEvaluator()
      .setLabelCol("is_date")
      .setRawPredictionCol("raw_prces")
      .setMetricName("areaUnderROC")
    val auc: Double = evaluator.evaluate(y_pred)
    println(auc)
  }

}
