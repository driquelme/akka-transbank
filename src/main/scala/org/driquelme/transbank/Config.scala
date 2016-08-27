package org.driquelme.transbank

import com.typesafe.config.ConfigFactory

/**
 * Created by danielriquelme on 25-04-16.
 */
object Config {
  val config = ConfigFactory.load()
  val serverConfig = config.getConfig("akka-transbank.server")
  val kccConfig = config.getConfig("akka-transbank.kcc")

  val serverAddress = serverConfig.getString("address")

  val serverPrefix = serverConfig.getString("prefix")
  val kccAddress = kccConfig.getString("address")
  val kccPort = kccConfig.getInt("port")
  val cgiBaseDir = kccConfig.getString("cgiBaseDir")

  val serverSoftware      =     serverConfig.getString("software")
  val serverName          =     serverConfig.getString("name")
  val serverPort          =     serverConfig.getInt("port")
}
