package com.qs.IOT

import com.alibaba.fastjson.JSON
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object IOTOnline {
  def main(args: Array[String]): Unit = {
    val spark:SparkSession = SparkSession
      //构建
      .builder()
      //设置master
      .master("local[2]")
      //设置程序名称：工作中自己定义，具有标示性，方便监控
      .appName(this.getClass.getSimpleName.stripSuffix("$"))
      //配置，设置shuffle的分区数
      .config("spark.sql.shuffle.partitions","2")
      .config("spark.testing.memory", "512000000")
      .getOrCreate()
    //调整日志级别
    spark.sparkContext.setLogLevel("WARN")
    //导包
    import spark.implicits._
    //todo 获取数据
    val inputData: DataFrame = spark.readStream
      .format("socket")
      .option("host", "hadoop01")
      .option("port", "9999")
      .load()
    //todo 处理数据
    val etl: Dataset[DeviceData] = inputData
      .as[String]
      .filter(line => null != line && line.trim.split(" ").length > 0)
      .mapPartitions(part=>{
        part.map(line=> {
          JSON.parseObject(line,classOf[DeviceData])
        })
      })
    /**
     * 使用离线和实时两种方式统计:
     * 1）、信号强度大于10的设备
     * 2）、各种设备类型的数量
     * 3）、各种设备类型的平均信号强度
     * {"device":"redmi",
     * "deviceType": "phone",
     * "signal": 10,
     * "time": "2018-01-02 15:20:00"}
     */
    //导入dataSet的专用高阶函数包
    import org.apache.spark.sql.expressions.scalalang.typed._
    val rsData: Dataset[(String, Long, Double)] = etl
      .filter(device => device.signal > 10)
      .groupByKey(device => device.deviceType)
      .agg(
        count(device => device.deviceType), avg(device => device.signal)
      )
    rsData
  //todo 输出数据
    val query: StreamingQuery = rsData
      .writeStream
        .outputMode(OutputMode.Complete())
      .format("console")
      .option("numRows", "10")
      .option("truncate", "false")
      .start()
    //todo 启动
    query.awaitTermination()


  }
}
//{"device":"redmi","deviceType": "phone","signal": 10,"time": "2018-01-02 15:20:00"}
case class DeviceData(device:String,deviceType:String,signal:Int,time:String)