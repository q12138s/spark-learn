package com.qs

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, TaskContext}

import scala.collection.mutable.ListBuffer

object SparkRddAggregation {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }


    val a1: Range.Inclusive = 1 to 10
    val dataRdd: RDD[Int] = sc.parallelize(a1,2)


    dataRdd.foreachPartition(part=>{
      println(s"partitionId = ${TaskContext.getPartitionId()} -\t${part.mkString(",")}")
    })

    dataRdd.reduce((temp,item)=>{
        println(s"partitionId = ${TaskContext.getPartitionId()} -\ttemp=${temp},item=${item}")
        temp+item
    })

      val result: ListBuffer[Int] = dataRdd.aggregate(new ListBuffer[Int])(
        //实现分区内的操作，求每个分区中最大的两个值
        (u, t) => {
          u += t
          val parTop2: ListBuffer[Int] = u.sorted.takeRight(2)
          parTop2
        },
        //将每个分区进行合并，取最大的两个结果
        (u1, u2) => {
          val buffer: ListBuffer[Int] = u1 ++ u2
          val rs: ListBuffer[Int] = buffer.sorted.takeRight(2)
          rs
        }
      )

      result.foreach(println)

    sc.stop()

  }

}
