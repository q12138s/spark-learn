package sparkMllib_classfication_day03.IrisDemo

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.stat.Correlation

/**
 * DESC: 使用SQL方式读取数据
 * Complete data processing and modeling process steps:
 * 1-准备环境
 * 2-准备读取数据---option方法读取
 * 3-解析数据
 * 4-打印schema
 */
object IrisSparkSQLStaticesDemo {
  def main(args: Array[String]): Unit = {
    //    * 1-准备环境
    val conf: SparkConf = new SparkConf().setAppName("IrisSparkCoreLoader").setMaster("local[*]").set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    import org.apache.spark.sql.functions._
    //    * 2-准备读取数据---option方法读取
    val datapath = "C:\\Users\\admin\\IdeaProjects\\spark\\sparkML_day03\\datas\\iris.csv"
    val data: DataFrame = spark.read.format("csv").option("header", "true").option("inferschema", true).load(datapath)
    //    * 3-解析数据
    data.printSchema()
    data.show(false)
    //    * 4-打印schema
    //    root
    //    |-- sepal_length: double (nullable = true)
    //    |-- sepal_width: double (nullable = true)
    //    |-- petal_length: double (nullable = true)
    //    |-- petal_width: double (nullable = true)
    //    |-- class: string (nullable = true)
    val vec: VectorAssembler = new VectorAssembler().setInputCols(Array("sepal_length", "sepal_width", "petal_length", "petal_width"))
      .setOutputCol("features")
    val vecResult: DataFrame = vec.transform(data)
    //Compute the Pearson correlation matrix for the input Dataset of Vectors.
    val corr: DataFrame = Correlation.corr(vecResult, "features", "pearson")
    println("corr matrix is:")
    corr.show(false)
  }
}