package com.qs.datatypes


import breeze.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object readLibsvm {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }
    sc.setLogLevel("WARN")
//    println(vector)

    val libData: RDD[LabeledPoint] = MLUtils.loadLibSVMFile(sc,"sparkML_day02/datas/loadLibSVMFile.txt")
    libData.foreach(println)


    sc.stop()

  }

}
