package com.qs.order

import kafka.serializer.StringDecoder
import org.apache.commons.lang.time.FastDateFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 基于借贷模式
 */
object SparkStreamOrderCheckPoint {

  //贷出函数，主要用于申请资源和回收资源
  def sparkProcess(args: Array[String])(process:StreamingContext=>Unit): Unit ={
    val checkpoint = "datas/spark/stream/chk/order02"
    var ssc: StreamingContext = null
    try {
      ssc = StreamingContext.getActiveOrCreate(
        checkpoint,
        () => {
        val conf: SparkConf = new SparkConf()
          .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
          .setMaster("local[4]")
          .set("spark.testing.memory", "512000000")
        val context = new StreamingContext(conf, Seconds(5))
          context.sparkContext.setLogLevel("WARN")
          context.checkpoint(checkpoint)
          process(context)
        context

      })
      //设置日志级别
      ssc.sparkContext.setLogLevel("WARN")
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
    val kafkaParams: Map[String, String] = Map("bootstrap.servers"->"hadoop01:9092,hadoop02:9092,hadoop03:9092","auto.offset.reset"->"largest")
    val topics: Set[String] = Set("test")
    val kafkaInput: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc,
      kafkaParams,
      topics
    )

    //todo:2-转换数据
    val dataStream: DStream[(Int, Double)] = kafkaInput
      .filter(line => null != line._2 && line._2.trim.split(",").length == 3)
      .map(tuple => {
        val Array(orderId, province, orderAmt) = tuple._2.split(",")
        (province.toInt,orderAmt.toDouble)
      })
    val rsStream: DStream[(Int, Double)] = dataStream
      .transform(rdd => {
        rdd
          .reduceByKey(_ + _)
      })
    //进行有状态的叠加
    val rs: DStream[(Int, Double)] = rsStream
      .updateStateByKey(
        (current: Seq[Double], previous: Option[Double]) => {
          val currentValue: Double = current.sum
          val previousValue: Double = previous.getOrElse(0.0)
          val lastValue = currentValue + previousValue
          Some(lastValue)
        }
      )

    //todo:3-输出数据
    rs
      .foreachRDD((rdd,time)=>{
//        if (! rdd.isEmpty()){
          val dateTime = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(time.milliseconds)
          println("======================")
          println(dateTime)
          println("======================")
          rdd.foreach(println)

//        }
      })
  }

  def main(args: Array[String]): Unit = {
    sparkProcess(args)(processData)

  }

}
