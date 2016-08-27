package org.driquelme.transbank

import java.io.File

import akka.http.scaladsl.model.headers.{`Content-Type`, `Remote-Address`}
import akka.http.scaladsl.model.{RemoteAddress, HttpRequest}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import org.driquelme.transbank.Config._
import scala.sys.process.{Process}

/**
 * Created by danielriquelme on 16-04-16.
 */
object Main extends App {


  override def main(args: Array[String]) {


    //Runtime.getRuntime.exec(Array("cgi-bin/tbk_bp_pago.cgi", "tbk_bp_pago.cgi"), env2)

    //val c = new CGIDirectives
    //val pb = c.build(Seq("/vagrant/cgi-bin/tbk_bp_pago.cgi", "tbk_bp_pago.cgi"), None, env.toSeq: _*)
    //pb.lineStream.map(println(_))

  }

}

class CGIDirectives() {

  import Config._

  def run(path: String, request: HttpRequest, params: Map[String, String]): Unit = {
    val contentLength = request.entity.contentLengthOption
    //val env = buildEnv("")
    //println(env)
  }

  def build(command: Seq[String], cwd: Option[File], extraEnv: (String, String)*): scala.sys.process.ProcessBuilder = {
    val jpb = new ProcessBuilder(command.toArray: _*)
    cwd foreach (jpb directory _)
    extraEnv foreach { case (k, v) => jpb.environment.put(k, v) }
    Process(jpb)
  }

  def runCmd(pb: ProcessBuilder) = {
    1
  }


}

object CGIDirectives {
  def buildEnv(path: String): Directive1[Map[String, String]] =
    extractRequest.flatMap { request =>
      extractClientIP.flatMap { rAddress =>
        optionalHeaderValueByName(`Content-Type`.name).flatMap { cType =>
          val remoteAddress = rAddress.toOption.map(`Remote-Address`.name -> _.getHostAddress)
          val remoteMethod = Some("REMOTE_METHOD" -> request._1.value)
          val contentLength = request.entity.contentLengthOption.map(("CONTENT_LENGTH" -> _.toString))
          val contentType = cType.map(`Content-Type`.name -> _)
          val scriptName = Some("SCRIPT_NAME" -> path)
          val pathInfo = Some("SCRIPT_NAME" -> path)
          val queryString = request._2.queryString().map("QUERY_STRING" -> _)
          val serverProtocol = Some("SERVER_PROTOCOL" -> request._5.value)
          val restOfHeaders = request.headers.map { h =>
            "HTTP_" + h.name.toUpperCase.replace('-', '_') -> h.value
          }

          val envElements: Map[String, String] = Seq(remoteAddress, remoteMethod, contentLength, contentType,
            scriptName, pathInfo, pathInfo, queryString, serverProtocol).flatten.toMap

          val env = Map(
            ("PATH", "/usr/local/bin:/usr/ucb:/bin:/usr/bin"),
            ("GATEWAY_INTERFACE", "CGI/1.1"),
            ("SERVER_SOFTWARE", serverSoftware),
            ("SERVER_NAME", serverName),
            ("SERVER_PORT", serverPort.toString),
            ("REQUEST_METHOD", request._1.value),
            ("SERVER_PROTOCOL", request._5.value)
            //("HTTP_REFERER", "")
          )

          provide(env ++ envElements)
        }
      }
    }
}

