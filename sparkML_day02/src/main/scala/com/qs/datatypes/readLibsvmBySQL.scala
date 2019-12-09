package com.qs.datatypes

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object readLibsvmBySQL {

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

    val libData: DataFrame = spark.read.format("libsvm").load("sparkML_day02/datas/loadLibSVMFile.txt")
    libData.printSchema()
    libData.show(false)

  }

}
