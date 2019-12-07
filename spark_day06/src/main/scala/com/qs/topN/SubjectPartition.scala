package com.qs.topN

import org.apache.spark.Partitioner

class SubjectPartition(subjects: Array[String]) extends Partitioner {
  private val index: Map[String, Int] = subjects.zipWithIndex.toMap
  override def numPartitions: Int = {
    subjects.length
  }

  override def getPartition(key: Any): Int = {
    val subject: String = key.asInstanceOf[(String,String)]._1
//    val subject: (String, Int) = key.asInstanceOf[(String,Int)]
    index(subject)
  }
}
