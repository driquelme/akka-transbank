import org.scalatest.WordSpecLike

import org.driquelme.transbank.CGIDirectives

/**
  * Created by danielriquelme on 16-04-16.
  */
class CommandRunnerTest extends WordSpecLike {

  val env = Map(
    ("SERVER_PROTOCOL", "HTTP/1.0"),
    ("SERVER_SOFTWARE", "SimpleHTTP/0.6 Python/2.7.3"),
    ("SCRIPT_NAME", "/cgi-bin/tbk_bp_pago.cgi"),
    ("REQUEST_METHOD", "GET"),
    ("QUERY_STRING", "TBK_MONTO=1000"),
    ("HTTP_REFERER", ""),
    ("SERVER_NAME", "precise64"),
    ("REMOTE_ADDR", "10.0.2.2"),
    ("SERVER_PORT", "8000"),
    ("CONTENT_LENGTH", ""),
    ("HTTP_USER_AGENT", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36"),
    ("HTTP_ACCEPT", " text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n"),
    ("GATEWAY_INTERFACE", "CGI/1.1"),
    ("CONTENT_TYPE", "text/plain"),
    ("REMOTE_HOST", ""),
    ("PATH_INFO", ""))


  // TBK_MONTO=1000000&TBK_TIPO_TRANSACCION=TR_NORMAL&TBK_ORDEN_COMPRA=1& TBK_ID_SESION=1&TBK_URL_EXITO=http://exito.html&TBK_URL_FRACASO=http://fracaso.html
  val datosPrueba1 = Seq(
    ("TBK_MONTO", "1000"), ("TBK_TIPO_TRANSACCION", "TR_NORMAL"), ("TBK_ORDEN_COMPRA", "1"),
    ("TBK_ID_SESION", "1"), ("TBK_URL_EXITO", "http://exito.html"), ("TBK_URL_FRACASO", "http://fracaso.html"))

  "CommandRunner" must {
    "be able to run an ls command" in {
      val c = new CGIDirectives
      c.build(Seq("ls"), None)
        .lineStream
        .map(println(_))
    }
    /*"call cgi" in {
      val c = new CommandRunner
      c.build(Seq("cgi-bin/tbk_bp_pago.cgi"), None, env.toSeq:_*)
        .lineStream
        .map(println(_))
    }*/
  }
}
