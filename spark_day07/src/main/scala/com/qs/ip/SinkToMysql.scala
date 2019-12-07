package com.qs.ip

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Properties

object SinkToMysql extends {

  def saveToMysql(part:Iterator[((String,String),Int)]): Unit ={
    //写JDBC
    //TODO-1：申请Driver类
    Class.forName("com.mysql.jdbc.Driver")
    //申明连接配置
    var conn:Connection = null
    var prep:PreparedStatement = null
    val url = "jdbc:mysql://haoop01:3306/test"
    val prop = new Properties()
    prop.put("user","root")
    prop.put("password","mysql")
    val sql = "insert into tb_ip_location(longitude,latitude,total_count) values(?,?,?)"
    try{
      conn = DriverManager.getConnection(url,prop)
      prep = conn.prepareStatement(sql)
      //迭代分区数据，赋值
      part.foreach{ case ((longitude,latitude),number)=> {
        //将经度赋值
        prep.setString(1,longitude)
        //将纬度赋值
        prep.setString(2,latitude)
        //将个数赋值
        prep.setInt(3,number)
        //添加到批处理
        prep.addBatch()
      }}
      //执行批处理
      prep.executeBatch()
    }catch {
      case e:Exception => e.printStackTrace()
    }finally {
      if(prep != null) prep.close()
      if(conn != null) conn.close()
    }
  }

}
