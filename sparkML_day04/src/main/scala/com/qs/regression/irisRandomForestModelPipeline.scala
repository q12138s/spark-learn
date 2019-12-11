package com.qs.regression

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object irisRandomForestModelPipeline {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    val usedDF: DataFrame = spark.read.format("csv").option("header", true).
      load("sparkML_day04/datas/iris.csv")

    usedDF.printSchema()
    val data: DataFrame = usedDF.select(
      usedDF("sepal_length").cast("double"),
      usedDF("sepal_width").cast("double"),
      usedDF("petal_length").cast("double"),
      usedDF("petal_width").cast("double"),
      usedDF("class").alias("classlabel")
    )
    val split: Array[Dataset[Row]] = data.randomSplit(Array(0.8, 0.2), seed = 1234L)
    val trainingSet = split(0)
    val testSet = split(1)
    //    * 4-特征工程----------特征抽取、特征转换、特征选择
    val strIndex: StringIndexer = new StringIndexer().setInputCol("classlabel").setOutputCol("index_classlabel")
    //特征组合
    val vec: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("sepal_length", "sepal_width", "petal_length", "petal_width"))
      .setOutputCol("features")

    //算法的超参数的选择 ---得到超参数，代入模型训练
    //模型训练
    //随机森林
    val randomForestClassifier: RandomForestClassifier = new RandomForestClassifier()
      .setLabelCol("index_classlabel")
      .setFeaturesCol("features")
      .setPredictionCol("prces")
      .setNumTrees(100)
      .setImpurity("gini")
      .setFeatureSubsetStrategy("log2")
      .setSubsamplingRate(0.8)
    //    * 7-模型预测---------------tranform
    val pipeline: Pipeline = new Pipeline().setStages(Array(strIndex, vec, randomForestClassifier))
    val pipelineModel: PipelineModel = pipeline.fit(trainingSet)
    val y_pred: DataFrame = pipelineModel.transform(testSet)
    val y_pred_train: DataFrame = pipelineModel.transform(trainingSet)
    //    * 8-模型校验---------------Evalutor
    val evaluator: MulticlassClassificationEvaluator = new MulticlassClassificationEvaluator().setLabelCol("index_classlabel").setMetricName("accuracy").setPredictionCol("prces")
    val accuracy: Double = evaluator.evaluate(y_pred)
    val accuracy_train: Double = evaluator.evaluate(y_pred_train)
    println("test accuracy result is:", accuracy)
    println("train accuracy result is:", accuracy_train)


  }

}
