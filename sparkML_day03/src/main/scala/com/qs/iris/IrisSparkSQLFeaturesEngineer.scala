package sparkMllib_classfication_day03.IrisDemo

import org.apache.spark.ml.feature._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}

/**
 * DESC: 使用SQL方式读取数据
 * Complete data processing and modeling process steps:
 * 1-准备环境
 * 2-准备读取数据---option方法读取
 * 3-解析数据
 * 4-打印schema
 */
object IrisSparkSQLFeaturesEngineer {
  def main(args: Array[String]): Unit = {
    //    * 1-准备环境
    val conf: SparkConf = new SparkConf().setAppName("IrisSparkCoreLoader").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    import spark.implicits._
    //    * 2-准备读取数据---option方法读取
    val datapath = "C:\\Users\\admin\\IdeaProjects\\spark\\sparkML_day03\\datas\\iris.csv"
    val data: DataFrame = spark.read.format("csv").option("header", "true").option("inferschema", true).load(datapath)
    //    * 3-解析数据
    data.printSchema()
    data.show(false)
    //    * 4-打印schema

    //1-首先将数据的标签列进行labelencoder的编码的操作0-1-2
    val strIndex: StringIndexer = new StringIndexer().setInputCol("class").setOutputCol("labelclass")
    val strModel: StringIndexerModel = strIndex.fit(data)
    val strResult: DataFrame = strModel.transform(data)
    strResult.show(false)
    //2-可以将4个特征列转化为3个特征列
    //2-1特征选择------df.secelt------ChiSquareSeletor
//    data.select("sepal_length").show(false)
//    data.select($"sepal_length").show(false)
//    data.select(col("sepal_length"))
//    data.select($"sepal_length", col("sepal_width")).show(false)
    val vec: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("sepal_length", "sepal_width", "petal_length", "petal_width"))
      .setOutputCol("features")
    val vecResult: DataFrame = vec.transform(data)

    val strIndex1: StringIndexer = new StringIndexer().setInputCol("class").setOutputCol("labelclass")
    val strModel1: StringIndexerModel = strIndex1.fit(vecResult)
    val strResult1: DataFrame = strModel1.transform(vecResult)
    println("strResult1")
    strResult1.show(false)
    strResult1.printSchema()

    //卡方验证选特征
    val chi: ChiSqSelector = new ChiSqSelector().setFeaturesCol("features").setLabelCol("labelclass").setNumTopFeatures(3)
    val chiModel: ChiSqSelectorModel = chi.fit(strResult1)
    val chiResult: DataFrame = chiModel.transform(strResult1)
    chiResult.show(false)
  //2-2特征降维------PCA
    println("pca transfomation:")
    val pca: PCA = new PCA().setInputCol("features").setOutputCol("pca_features").setK(3)
    val pcaModel: PCAModel = pca.fit(vecResult)
    val pcaRE: DataFrame = pcaModel.transform(vecResult)
   val pca1: PCA = new PCA().setInputCol("pca_features").setOutputCol("pca11_features").setK(2)
    val pcaModel1: PCAModel = pca1.fit(pcaRE)
    pcaModel1.transform(pcaRE).show(false)
  }
}
