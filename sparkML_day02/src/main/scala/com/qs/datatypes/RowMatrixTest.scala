package com.qs.datatypes

import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix, RowMatrix}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object RowMatrixTest {

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

    val rdd= sc.textFile("sparkML_day02/datas/RowMatrix.txt")
      .map(_.split(" ").map(_.toDouble))
      .map(Vectors.dense(_))
      .map(x=>new IndexedRow(x.size,x))
    val matrix = new IndexedRowMatrix(rdd)
    println(matrix.getClass)
    println(matrix.rows.foreach(println))



  }

}
