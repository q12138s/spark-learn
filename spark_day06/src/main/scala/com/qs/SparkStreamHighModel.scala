package com.qs

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 基于借贷模式
 */
object SparkStreamHighModel {

  //贷出函数，主要用于申请资源和回收资源
  def sparkProcess(args: Array[String])(process:StreamingContext=>Unit): Unit ={
    var ssc: StreamingContext = null
    try {
      ssc = StreamingContext.getActiveOrCreate(() => {
        val conf: SparkConf = new SparkConf()
          .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
          .setMaster("local[4]")
          .set("spark.testing.memory", "512000000")
        val context = new StreamingContext(conf, Seconds(5))
        context
      })
      //设置日志级别
      ssc.sparkContext.setLogLevel("WARN")
      //调用处理的方法来执行
      process(ssc)
      //启动程序
      ssc.start()
      //持久运行
      ssc.awaitTermination()
    }catch {
      case e:Exception => e.printStackTrace()
    }finally {
      if (null != ssc) ssc.stop(true,true)
    }
  }

  //用于处理数据的逻辑代码
  def processData(ssc:StreamingContext): Unit ={
    //todo:1-读取数据
    //todo:2-转换数据
    //todo:3-输出数据
  }

  def main(args: Array[String]): Unit = {
    sparkProcess(args)(processData)

  }

}
