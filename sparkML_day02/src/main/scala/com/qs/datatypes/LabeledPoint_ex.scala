package com.qs.datatypes

import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

object LabeledPoint_ex {

  def main(args: Array[String]): Unit = {

    val vector: linalg.Vector = Vectors.dense(1.0,2.0,3.0)
    val point = LabeledPoint(1.0,vector)
    println(point.label)
    println(point.features)


  }

}
