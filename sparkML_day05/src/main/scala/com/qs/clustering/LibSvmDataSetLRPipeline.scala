package com.qs.clustering

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

object LibSvmDataSetLRPipeline {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]")
      .set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    val data: DataFrame = spark.read.format("libsvm").load("sparkML_day05/datas/sample_libsvm_data.txt")
    data.printSchema()

    //特征工程
    //训练模型
    val regression: LogisticRegression = new LogisticRegression()
      .setFeaturesCol("features")
      .setLabelCol("label")
      .setMaxIter(20)
      .setElasticNetParam(0.5)
      .setPredictionCol("prces")
      .setThreshold(0.5)

    val pipeline: Pipeline = new Pipeline().setStages(Array(regression))
    val pipelineModel: PipelineModel = pipeline.fit(data)

    //预测
    val y_pred: DataFrame = pipelineModel.transform(data)
    //模型校验
    val evaluator: MulticlassClassificationEvaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prces")
      .setMetricName("accuracy")
    val accuracy: Double = evaluator.evaluate(y_pred)
    println(accuracy)
    pipelineModel.stages(0).explainParams().foreach(print(_))


  }

}
