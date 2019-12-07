package com.qs

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkRddJoin {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }
    val dept: RDD[String] = sc.textFile("spark_day01/datas/join/dept.txt",2)
    val emp: RDD[String] = sc.textFile("spark_day01/datas/join/emp.txt")

    val empRdd: RDD[(String, String)] = emp
      .map(line => {
        val arr: Array[String] = line.split("\t")
        val key = arr(7)
        val value = arr(0) + "\t" + arr(1)
        (key, value)
      })

    val deptRdd: RDD[(String, String)] = dept
      .map(line => {
        val arr: Array[String] = line.split("\t")
        val key = arr(0)
        val value = arr(1) + "\t" + arr(2)
        (key, value)
      })

    val join: RDD[(String, (String, String))] = deptRdd.join(empRdd)
    //输出数据
    join.foreach{
      case (deptno,(empInfo,deptInfo)) => println(s"${deptno}\t${empInfo}\t${deptInfo}")
    }



    sc.stop()

  }

}
