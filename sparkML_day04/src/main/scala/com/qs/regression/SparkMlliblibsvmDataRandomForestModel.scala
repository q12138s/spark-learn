package com.qs.regression

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object SparkMlliblibsvmDataRandomForestModel {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    val data: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc,"sparkML_day04/datas/sample_libsvm_data.txt")
    val Array(trainingSet,testSet): Array[RDD[LabeledPoint]] = data.randomSplit(Array(0.8,0.2),seed = 123L)
    //    * 4-准备算法
    //    input: RDD[LabeledPoint],
    //    numClasses: Int,
    val numClasses=2
    //    categoricalFeaturesInfo: Map[Int, Int],
    val categoricalFeaturesInfo=Map[Int,Int]()
    //    numTrees: Int,
    val numTrees=20
    //    featureSubsetStrategy: String,
    val featureSubsetStrategy="sqrt" //根号下的特征的个数
    //    impurity: String,
    val impurity="gini"
    //    maxDepth: Int,
    val maxDepth=4
    //    maxBins: Int,
    val maxBins=20
    //    * 5-模型训练
    val randomForestModel: RandomForestModel = RandomForest.trainClassifier(trainingSet,numClasses,categoricalFeaturesInfo,numTrees,featureSubsetStrategy,impurity,maxDepth,maxBins)
    //    * 6-模型的预测
    val labelAndPred=testSet.map{point=>{
      val y_pred= randomForestModel.predict(point.features)
      (point.label,y_pred)
    }
    }
    //sparkMlib
    val accuracy: Double = labelAndPred.filter(r=>(r._1==r._2)).count().toDouble/testSet.count()
    println("testSet accuracy:",accuracy)
  }


}
