package com.qs.kafka010

import org.apache.commons.lang.time.FastDateFormat
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

object wc {
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
    val locationStrategy: LocationStrategy = LocationStrategies.PreferConsistent
    val topics: Set[String] = Set("test")
    val kafkaParams: collection.Map[String, Object] = Map("bootstrap.servers" -> "hadoop01:9092,hadoop02:9092,hadoop03:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "bigdata",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean))
    val consumerStrategy: ConsumerStrategy[String, String] =
      ConsumerStrategies.Subscribe(
        topics,
        kafkaParams
      )
    val kafkaStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      locationStrategy, //计算最优位置
      consumerStrategy //指定kafka的连接属性
    )

    //todo:2-转换数据
    val rsStream: DStream[(String, Int)] = kafkaStream
      .transform(rdd => {
        rdd
          .filter(line => null != line.value() && line.value().trim.split(" ").length > 0)
          .flatMap(_.value().trim.split(" "))
          .map((_, 1))
          .reduceByKey(_ + _)
      })
    //todo:3-输出数据
    rsStream
      .foreachRDD((rdd,time)=>{
        if (!rdd.isEmpty()){
          println("========================")
          println(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(time.milliseconds))
          println("========================")
          rdd.foreach(println)
        }
      })
  }

  def main(args: Array[String]): Unit = {
    sparkProcess(args)(processData)

  }
}
