package com.qs

import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.{Matrices, Matrix, Vectors}

object test {
  def main(args: Array[String]): Unit = {
    val vector: linalg.Vector = Vectors.dense(1.0,2.0,0)
    val vector1: linalg.Vector = Vectors.sparse(3,Array(2),Array(7))
//    println(vector)
//    println(vector1)
    val dm: Matrix = Matrices.dense(3, 2,Array(1.0, 3.0, 5.0, 2.0, 4.0, 6.0))
   // println(dm)
    // Create a sparse matrix ((9.0, 0.0), (0.0, 8.0), (0.0, 6.0))
    val sm: Matrix = Matrices.sparse(3, 2,Array(0, 1, 3),Array(0, 2, 1),Array(9, 6, 8))
    println(sm(2,1))
  }
}
