package com.qs.regression

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, StringIndexerModel, VectorAssembler}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit, TrainValidationSplitModel}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
object irisRandomForestModelCrossValition {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    val usedDF: DataFrame = spark.read.format("csv").option("header", true).
      load("sparkML_day04/datas/iris.csv")
    val data: DataFrame = usedDF.select(
      usedDF("sepal_length").cast("double"),
      usedDF("sepal_width").cast("double"),
      usedDF("petal_length").cast("double"),
      usedDF("petal_width").cast("double"),
      usedDF("class").alias("classlabel")) //StringIndexer
    data.printSchema()
    //    * 4-特征工程----------特征抽取、特征转换、特征选择
    val strIndex: StringIndexer = new StringIndexer().setInputCol("classlabel").setOutputCol("index_classlabel")
    val strModel: StringIndexerModel = strIndex.fit(data)
    val strResult: DataFrame = strModel.transform(data)
    //特征组合
    val vec: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("sepal_length", "sepal_width", "petal_length", "petal_width"))
      .setOutputCol("features")
    val vecResult: DataFrame = vec.transform(strResult)
    vecResult.show()
    //    * 5-算法的超参数的选择----得到超参数，代入模型训练
    //    * 6-模型训练----------------fit
    val split: Array[Dataset[Row]] = vecResult.randomSplit(Array(0.8, 0.2), seed = 1234L)
    val trainingSet = split(0)
    val testSet = split(1)
    //
    val randomForestClassifier: RandomForestClassifier = new RandomForestClassifier()
      .setLabelCol("index_classlabel")
      .setFeaturesCol("features")
      .setPredictionCol("prces")
      .setNumTrees(100)
      .setImpurity("gini")
      .setFeatureSubsetStrategy("log2")
      .setSubsamplingRate(0.8)
    val model2: RandomForestClassificationModel = randomForestClassifier.fit(trainingSet)
    //val rfmModel: RandomForestClassificationModel = randomForestClassifier.fit(trainingSet)
    //    * 7-模型预测---------------tranform
    //    val y_pred: DataFrame = rfmModel.transform(testSet)
    //    val y_pred_train: DataFrame = rfmModel.transform(trainingSet)
    //    * 8-模型校验---------------Evalutor
    val evaluator: MulticlassClassificationEvaluator = new MulticlassClassificationEvaluator().setLabelCol("index_classlabel").setMetricName("accuracy").setPredictionCol("prces")
    //    val accuracy: Double = evaluator.evaluate(y_pred)
    //    val accuracy_train: Double = evaluator.evaluate(y_pred_train)
    //    println("test accuracy result is:", accuracy)
    //    println("train accuracy result is:", accuracy_train)
    //    * 9-模型保存等-------------model.save
    //    rfmModel.save()
    //    RandomForestClassificationModel.load()
    val param: Array[ParamMap] = new ParamGridBuilder()
      .addGrid(randomForestClassifier.impurity, Array("entropy", "gini"))
      .addGrid(randomForestClassifier.featureSubsetStrategy, Array("sqrt", "onethird"))
      .addGrid(randomForestClassifier.subsamplingRate, Array(0.4, 0.5, 0.6))
      .build()
    val evaluator1: TrainValidationSplit = new TrainValidationSplit()
      .setTrainRatio(0.8)
      .setEstimatorParamMaps(param)
      .setEstimator(randomForestClassifier)
      .setEvaluator(evaluator)
    val model1: TrainValidationSplitModel = evaluator1.fit(trainingSet)
    val y_pred1: DataFrame = model1.transform(testSet)
    val accuracy1: Double = evaluator.evaluate(y_pred1)
    println(accuracy1)//0.9583333333333334
    //以下是训练的超参数的部分，得到的超参数可以用于重新训练模型
    val extractParamMap: ParamMap = model1.extractParamMap()
    println(extractParamMap)

    val rfmModel = model1.bestModel.asInstanceOf[RandomForestClassificationModel]
    println(rfmModel.extractParamMap)


  }

}
