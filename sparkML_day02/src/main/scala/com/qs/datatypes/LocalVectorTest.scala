package com.qs.datatypes

import org.apache.spark.ml.linalg
import org.apache.spark.ml.linalg.Vectors

object LocalVectorTest {

  def main(args: Array[String]): Unit = {

    val vector: linalg.Vector = Vectors.sparse(3,Seq((0,3.0),(1,2.0)))
    println(vector)

  }

}
