package com.qs.status

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamWithStatus {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
      .set("spark.testing.memory", "512000000")
    //第一个传递conf对象，用于创建SparkContext,第二个参数：Batch Interval，每批次获取数据的间隔
    val ssc = new StreamingContext(conf,Seconds(2))
    ssc.sparkContext.setLogLevel("WARN")
    ssc.checkpoint("datas/spark/stream/update-"+System.currentTimeMillis())
    //TODO-1:读取数据
    val inputStream: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop01",6666,StorageLevel.MEMORY_AND_DISK_SER)
    //TODO-2:处理数据
    val tempDStream: DStream[(String, Int)] = inputStream.transform( rdd =>{
      //对每个RDD进行操作
      rdd
        //过滤
        .filter(line => null != line && line.trim.length > 0)
        //取出每个单词
        .flatMap(line => line.trim.split(" "))
        //构建二元组
        .map((_,1))
        //聚合
        .reduceByKey(_+_)
    })
    //TODO-3：输出数据
    /**
     * Seq[V]：当前Key在当前批次统计的结果
     * Option[S]：当前Key在之前处理的结果中的值，如果有值返回Some，如果没有值返回None
     */
    val rs: DStream[(String, Int)] = tempDStream
      .updateStateByKey(
        (currentValue: Seq[Int], previouse: Option[Int]) => {
          val current = currentValue.sum
          //获取上一次的值
          val preValue: Int = previouse.getOrElse(0)
          //最终的 结果值
          val lastValue: Int = current + preValue
          //返回
          Some(lastValue)
        })
    rs.foreachRDD((rdd,time)=>{
      if (!rdd.isEmpty()){
        println("======================")
        println(s"Time:$time")
        println("======================")
        rdd.foreach(println)
      }
    })
    //启动程序
    ssc.start()
    //让程序保持持久运行，除非遇到手动关闭或者异常
    ssc.awaitTermination()
    //关闭资源（Context,优雅的关闭其他资源）
    ssc.stop(true,true)
  }
}
