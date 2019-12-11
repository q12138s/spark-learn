package com.qs.iris

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature._
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.tuning.{CrossValidator, CrossValidatorModel, ParamGridBuilder}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
object irisClaasifitionModelVersionPipelilneCrossValidation {

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
    //    * 5-特征工程
    // 5-1 标签列的处理 StringIndexer
    val indexer: StringIndexer = new StringIndexer().setInputCol("class").setOutputCol("classlabel")
    // VectorAssembler
    val vec: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("sepal_length", "sepal_width", "petal_length", "petal_width"))
      .setOutputCol("features")
    // 5-3 ChisquareTest
    val chi: ChiSqSelector = new ChiSqSelector()
      .setLabelCol("classlabel")
      .setFeaturesCol("features")
      .setNumTopFeatures(2)
      .setOutputCol("chiTest")
    // 5-4 StandSclear
    // StandSclear是对 数值型数据的归一化减去均值除以方差---StandSclear内部传入的是vector的类型,必须在前面实现VectorAssemble
    val std: StandardScaler = new StandardScaler().setInputCol("features").setOutputCol("stand_features")
    // 5-4构建决策树模型DecisionTreeClassifier
    val dtc: DecisionTreeClassifier = new DecisionTreeClassifier()
      .setLabelCol("classlabel")
      .setFeaturesCol("stand_features")
      .setPredictionCol("prces")
      .setImpurity("entropy")
    //    * 6-模型训练==算法+数据
    val pipeline: Pipeline = new Pipeline().setStages(Array(indexer, vec, chi, std, dtc))
    //0indexer-1vec-2chi-3std-4dtc
    //    val pipelineModel: PipelineModel = pipeline.fit(trainSet)
    //    * 7-模型预测
    //    val y_pred: DataFrame = pipelineModel.transform(testSet)
    //    * 8-模型校验
    val evaluator: MulticlassClassificationEvaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("classlabel").setPredictionCol("prces").setMetricName("accuracy")
    //超参数选择
    val parambiilder: Array[ParamMap] = new ParamGridBuilder()
      .addGrid(std.withMean, Array(true, false))
      .addGrid(dtc.impurity, Array("entropy", "gini"))
      .addGrid(dtc.maxDepth, Array(3, 5, 7))
      .build()
    //交叉验证+网格搜索
    val cross: CrossValidator = new CrossValidator()
      .setEstimator(pipeline)
      .setEstimatorParamMaps(parambiilder)
      .setEvaluator(evaluator)
      .setNumFolds(3)
    val mode: CrossValidatorModel = cross.fit(trainSet)
    mode.transform(testSet).show(false)
    mode.transform(trainSet).show(false)

    //以下是训练的超参数的部分，得到的超参数可以用于重新训练模型
    val map: ParamMap = mode.extractParamMap()
    println(map)

    val model: PipelineModel = mode.bestModel.asInstanceOf[PipelineModel]
    println(model.stages(3).extractParamMap)
    println(model.stages(4).extractParamMap)

  }

}
