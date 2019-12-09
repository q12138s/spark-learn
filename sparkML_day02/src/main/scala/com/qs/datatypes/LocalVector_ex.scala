package com.qs.datatypes

import org.apache.spark.ml.linalg
import org.apache.spark.ml.linalg.Vectors

object LocalVector_ex {

  def main(args: Array[String]): Unit = {

    val vector: linalg.Vector = Vectors.dense(1.0,2.0,3.0)
    println(vector)
    val vector1: linalg.Vector = Vectors.sparse(4,Array(0,2,3),Array(1,2,3))
    println(vector1(1))

  }

}
