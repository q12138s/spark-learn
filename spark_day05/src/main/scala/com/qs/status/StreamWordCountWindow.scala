package com.qs.status

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamWordCountWindow {

  def main(args: Array[String]) {
    /**
     * 初始化环境：SparkContext
     */
    val BATCH_INTERVAL:Int = 2//每批次获取数据的间隔
    val SLIDE_INTERVAL:Int = 2//处理的 时间间隔，隔2s处理一次
    val WINDOW_INTERVAL :Int = 4 //处理多长时间的数据，窗口的大小
    // Create a local StreamingContext with two working thread and batch interval of 1 second.
    // The master requires 2 cores to prevent from a starvation scenario.
    //创建一个SparkConf对象
    val conf = new SparkConf()
      .setMaster("local[2]")
      .set("spark.testing.memory", "512000000")
      .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
    //创建一个StreamingContext对象，内部包含了一个SparkContext对象
    val ssc = new StreamingContext(conf, Seconds(2))//第一个传递conf对象，用于创建SparkContext,第二个参数：Batch Interval，每批次获取数据的间隔
    //设置日志级别
    ssc.sparkContext.setLogLevel("WARN")



    /**
     * 处理数据
     */
    //Todo-1：读取数据
    val inputStream: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop01",9999,StorageLevel.MEMORY_AND_DISK_SER)
    //Todo-2：处理数据
    //在实际的 处理之前，先设置窗口，来告诉程序，隔多长时间处理一次，每次处理多长时间的数据
    //第一个是窗口大小，第二个是滑动大小
    val window: DStream[String] = inputStream.window(Seconds(4),Seconds(2))
    /**
     * 对窗口中的每个RDD来进行转换
     * \def transform[U: ClassTag](transformFunc: RDD[T] => RDD[U]):
     */
    val rs: DStream[(String, Int)] = window.transform( rdd =>{
      //对每个RDD进行操作
      rdd
        //过滤
        .filter(line => null != line && line.trim.length > 0)
        //取出每个单词
        .flatMap(line => line.trim.split(" "))
        //构建二元组
        .map((_,1))
        //聚合
//        .reduceByKey(_+_)
    })

    val rs1: DStream[(String, Int)] = rs.reduceByKeyAndWindow(
      (temp: Int, item: Int) => temp + item,
      Seconds(4),
      Seconds(2)
    )


    //Todo-3：输出数据
    /**
     * 对每个RDD进行输出
     * 第一个参数就是当前的RDD
     * 第二个参数是该批次的处理时间
     */
    rs1.foreachRDD((rdd,time)=>{
      //对每个RDD来进行输出，并且获取该RDD处理的时间
      //判断当前批次的数据是否为空，为空不输出
      if(!rdd.isEmpty()){
        println("====================================")
        println(s"Time: $time")
        println("====================================")
        rdd.foreach(println)
      }

    })
    //Todo-3：输出数据

    /**
     * 启动程序，当意外关闭时关闭资源
     */
    //启动整个程序
    ssc.start()
    //让程序保持持久运行，除非遇到手动关闭或者异常
    ssc.awaitTermination()
    //关闭资源
    ssc.stop(true,true)//关闭Context，并优雅的关闭其他资源

  }

}