package com.qs.feature_transform

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.{IndexToString, OneHotEncoder, StringIndexer, StringIndexerModel}
import org.apache.spark.sql.{DataFrame, SparkSession}

object StringToIndexerTest {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf()
      .setMaster(this.getClass.getSimpleName.stripSuffix("$"))
      .setMaster("local[2]")
      .set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession
      .builder()
      .config(conf)
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    val data: DataFrame = spark.createDataFrame(Seq((0, "a"), (1, "b"), (2, "c"), (3, "a"), (4, "a"), (5, "c"))).toDF("id", "category")
    data.printSchema()
    data.show(false)

    val indexer: StringIndexer = new StringIndexer()
      .setInputCol("category")
      .setOutputCol("category_index")
    val model: StringIndexerModel = indexer.fit(data)
    val rs: DataFrame = model.transform(data)
    rs.show(false)

    val string: IndexToString = new IndexToString()
      .setInputCol("category_index")
      .setOutputCol("before")
      .setLabels(model.labels)
    string.transform(rs).show(false)

    val encoder: OneHotEncoder = new OneHotEncoder()
      .setInputCol("category_index")
      .setOutputCol("oneHot")
      .setDropLast(false)
    encoder.transform(rs).show(false)
    spark.stop()
  }

}
