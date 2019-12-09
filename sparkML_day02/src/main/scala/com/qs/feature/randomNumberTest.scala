package com.qs.feature

import org.apache.spark.mllib.random.RandomRDDs
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object randomNumberTest {

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

    //随机数
    val double: RDD[Double] = RandomRDDs.normalRDD(sc,10)
    double.foreach(println)
    //samples
    val dataSamples: RDD[Int] = sc.parallelize(1 to 10)
    //是否进行又放回的抽样------是否保证整体的数据是一致的
    //抽样的比例，抽样20%
    //抽样的时候保证每次抽取的结果的可重复性seed=3----随机数种子
    //同一个seed随机数抽样的结果是一致的
    val sample: RDD[Int] = dataSamples.sample(false,0.2,3)
    val sample1: Array[Int] = dataSamples.takeSample(false,2,40)

  }

}
