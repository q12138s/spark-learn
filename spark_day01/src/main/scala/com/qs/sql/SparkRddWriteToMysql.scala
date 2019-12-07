package com.qs.sql


import java.sql.DriverManager
import java.util.Properties
import java.sql.{PreparedStatement, DriverManager, Connection}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkRddWriteToMysql {

  def writeToMysql(part:Iterator[(String,Int)]): Unit = {
    Class.forName("com.mysql.jdbc.Driver")
    var conn: Connection = null
    var prep: PreparedStatement = null
    val url = "jdbc:mysql://hadoop01:3306/test"
    val prop = new Properties()
    prop.put("user", "root")
    prop.put("password", "mysql")
    val sql = "insert into wc(word,count) values(?,?)"
    try{
      conn = DriverManager.getConnection(url,prop)
      prep = conn.prepareStatement(sql)
      //迭代分区数据，赋值
      part.foreach( tuple => {
        //将单词赋值
        prep.setString(1,tuple._1)
        //将个数赋值
        prep.setInt(2,tuple._2)
        //添加到批处理
        prep.addBatch()
      })
      //执行批处理
      prep.executeBatch()
    }catch {
      case e:Exception => e.printStackTrace()
    }finally {
      if(prep != null) prep.close()
      if(conn != null) conn.close()
    }
  }

  def main(args: Array[String]): Unit = {

    val sc: SparkContext = {
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }

    val inputRdd: RDD[String] = sc.textFile("spark_day01/datas/wordcount/input/wordcount.data")
    val wcRdd: RDD[(String, Int)] = inputRdd
      .filter(line => null != line && line.trim.length > 0)
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
    val rsRdd: RDD[(String, Int)] = wcRdd.coalesce(1)
    rsRdd.foreachPartition(line => writeToMysql(line))


    sc.stop()

  }

}
