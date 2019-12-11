package com.qs.love
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
object LovaDataModel {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("LovaDataModel").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    val inputData: RDD[String] = sc.textFile("sparkML_day04/datas/loveTrain.txt")
    //解析数据
    val usedData: RDD[LabeledPoint] = inputData.map({
      line =>
        val str = line.split(",")
        LabeledPoint(str(0).toDouble, Vectors.dense(str(1).split(" ").map(_.toDouble)))
    })
    //划分训练测试集
    val Array(traingSet,testSet): Array[RDD[LabeledPoint]] = usedData.randomSplit(Array(0.8,0.2))
    //引入决策树算法
    val numClasses: Int = 2
    val categoricalFeaturesInfo: Map[Int, Int] = Map[Int,Int]()
    val impurity: String = "entropy"
    val maxDepth: Int = 5
    val maxBins: Int = 5
    //构建决策树模型
    val model: DecisionTreeModel = DecisionTree.trainClassifier(traingSet,numClasses,categoricalFeaturesInfo,impurity,maxDepth,maxBins)
    //模型预测
    val labelAndPred: RDD[(Double, Double)] = testSet.map({
      point =>
        val pred: Double = model.predict(point.features)
        (point.label, pred)
    })
    //校验
    val accuracy: Double = labelAndPred.filter(x=>(x._1)==(x._2)).count().toDouble/testSet.count()
    println(accuracy)

    //打印树的结构
    println(model.toDebugString)
  }

}
