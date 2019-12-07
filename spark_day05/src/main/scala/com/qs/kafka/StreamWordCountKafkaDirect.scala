package com.qs.kafka

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamWordCountKafkaDirect {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
      .set("spark.testing.memory", "512000000")
    //第一个传递conf对象，用于创建SparkContext,第二个参数：Batch Interval，每批次获取数据的间隔
    val ssc = new StreamingContext(conf,Seconds(2))
    ssc.sparkContext.setLogLevel("WARN")
    //TODO-1:读取数据
    val kafkaParam: Map[String, String] = Map("bootstrap.servers"->"hadoop01:9092,hadoop02:9092,hadoop03:9092","auto.offset.reset"->"largest")
    val topic: Set[String] = Set("test")
    val kafkaDStream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc,
      kafkaParam,
      topic
    )
    kafkaDStream

    //TODO-2:处理数据
    val inputDstream: DStream[String] = kafkaDStream
      .transform(rdd => {
        rdd
          .map(_._2)
          .filter(line => null != line && line.trim.length > 0)

      })
    val rs: DStream[(String, Int)] = inputDstream.transform(
      rdd => {
        rdd
          .flatMap(_.split(" "))
          .map((_, 1))
          .reduceByKey(_ + _)
      }
    )

    //TODO-3：输出数据
    rs.foreachRDD((rdd,time)=>{
      if (!rdd.isEmpty()){
        println("=====================")
        println(s"Time:${time}")
        println("=====================")
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
