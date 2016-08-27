package org.driquelme.transbank

import java.io.PrintWriter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Host, RawHeader}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.driquelme.transbank.Data._
import views.Payment

import scala.io.StdIn

/**
  * Created by danielriquelme on 15-04-16.
  */
object Boot {
  implicit val system = ActorSystem("akka-transbank")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  import sys.process._

  import Config._


  case class PaymentInfo(sessionId: String, key: String, secret: String, orderNumber: String,
                         purchaseAmount: Double, successUrl: String,
                         failureUrl: String, confirmationURL: String,
                         acknowledgeURL: String,
                         userDisplayName: Option[String],
                         purchaseDescription: Option[String],
                         currencyId: String) {
    def query = Seq(s"sessionId = $sessionId",
      s"orderNumber = $orderNumber",
      s"purchaseAmount = $purchaseAmount",
      s"successUrl = $successUrl",
      s"failureUrl = $failureUrl",
      s"confirmationURL = $confirmationURL",
      s"acknowledgeURL = $acknowledgeURL",
      s"userDisplayName = $userDisplayName").mkString("&")
  }

  case class KccConfirmation(response: String, orderNumber: String, purchaseAmount: String, sessionId: String,
                             transactionType: String, cardFinalNumbers: String, accountingDate: String, txDate: String,
                             txTime: String, txId: String, authorizationCode: String, paymentType: String,
                             installments: String, vci: String, mac: String)

  def paymentParameters(tipoTransaccion: String, paymentInfo: PaymentInfo) = FormData(
    ("TBK_TIPO_TRANSACCION", tipoTransaccion),
    ("TBK_MONTO", paymentInfo.purchaseAmount.toString),
    ("TBK_ORDEN_COMPRA", paymentInfo.orderNumber),
    ("TBK_ID_SESION", paymentInfo.sessionId),
    ("TBK_URL_EXITO", paymentInfo.successUrl),
    ("TBK_URL_FRACASO", paymentInfo.failureUrl)
  )

  case class TransactionFailure(uri: String, message: String) extends Exception

  val paymentRoute =
    get {
      path("test") {
        complete("ok")
      }
    } ~
      post {
        pathPrefix("payment") {
          path("start") {
            formFields('sessionId, 'key, 'secret, 'orderNumber, 'purchaseAmount.as[Double], 'successUrl,
              'failureUrl, 'confirmationURL, 'acknowledgeURL, 'userDisplayName.?,
              'purchaseDescription.?, 'currencyId).as(PaymentInfo) { paymentInfo =>

              val cgi = paymentInfo.currencyId match {
                case "CLP" => "cgi-bin/clp"
                case "USD" => "cgi-bin/usd"
                case _ => "cgi-bin"
              }

              //Check if order number is not already processed
              Route { ctx =>
                Transactions.findByOrderNumberAndStatus(paymentInfo.orderNumber, Transactions.ACCEPTED).flatMap {
                  case Some(transaction) =>
                    //Transaction exists and is accepted. Redirect to failure page
                    //ctx.complete(Util.redirectMessage(paymentInfo.failureUrl, s"Error. Payment for order ${paymentInfo.orderNumber} has already been processed"))

                    //This is just to comply with certification
                    ctx.complete(
                      HttpEntity(ContentTypes.`text/html(UTF-8)`,
                        Payment.kccPaymentForm(kccAddress, cgi, kccPort,
                          paymentInfo.copy(purchaseAmount = paymentInfo.purchaseAmount * 100))))


                  case None => {
                    //Transaction wasn't found with status ACCEPTED
                    Transactions.insert(Transaction.fromPaymentInfo(paymentInfo)).flatMap { t =>
                      ctx.complete(
                        HttpEntity(ContentTypes.`text/html(UTF-8)`,
                          Payment.kccPaymentForm(kccAddress, cgi, kccPort,
                            paymentInfo.copy(purchaseAmount = paymentInfo.purchaseAmount * 100))))
                    }
                  }
                }
              }
            }
          } ~
            path("confirm") {
              /*
              TBK_ORDEN_COMPRA=1231231&
              TBK_TIPO_TRANSACCION=TR_NORMAL&
              TBK_RESPUESTA=0&
              TBK_MONTO=100000&
              TBK_CODIGO_AUTORIZACION=108683&
              TBK_FINAL_NUMERO_TARJETA=6623&
              TBK_FECHA_CONTABLE=0426&
              TBK_FECHA_TRANSACCION=0426&
              TBK_HORA_TRANSACCION=224606&
              TBK_ID_SESION=11234&
              TBK_ID_TRANSACCION=172458117&
              TBK_TIPO_PAGO=VN&
              TBK_NUMERO_CUOTAS=0&
              TBK_VCI=TSY&
              TBK_MAC=71983aea17b2bf8474c0a4c0fdae1f789e51e6847c3b340250a34954fdc70528f92bf8089b19386ec23376ecbca1fc79ff5ec298cfb376b25963ec1c855d035f4e6ad9d13ed7ed538c32f5d1749dc60238ec2b591026a7ec52ad0429e1e91e4240b0718b7af25325a195e2ae7036d04cdebb7ac5791ba245f629d47d13ce7a91b2d8f2e25f3e3bb3d6af86eee046dc1302afcf111897b0322c195f954478e4ba621df6510e1857b5d33cc66672f31caba3ddf8c590d138db7c81d706e22d8b9a2fa37d03d73699689bfcd767444ac6eed2018e78c7ba72d5df8088b4f3607ff824dbf8292c0126fac0d64adebb966688e6fdffa9177a747477ff86a309e6049c6cb1a33576544d496c34898d744791d1e468b9036c2e9051e5aac9f11221e5e0e29bc0f9b62b84841f497a4e2605c3e20dd65e2492653e68ccc652be5374a0873ea59056ebc9a06ccd4852864bf0f04a325eccbedc67fede4b6df5e5dff8a51c18d409a18a721dcb2ff321e9784aae39dcab9071af1b0eb647d65707dbb8dd3bd36ebbeb3debc04ae7990c361e5530a67112f14a1840b675d2afde872eca40b52e6556d682f766ef5f50676cd6cc94944d4a2ce89fde0e9e4dd67f94e2715e323bf6d97217ecd6190ea91b0de5e1ebdcbbfd931cd1f34577ada2a546e1bce325f65ff58d7f9f41cb31261f9ed3481e61369070dabcc7b462a9ff54b5e776a758
              */
              formFields('TBK_RESPUESTA, 'TBK_ORDEN_COMPRA, 'TBK_MONTO, 'TBK_ID_SESION, 'TBK_TIPO_TRANSACCION, 'TBK_FINAL_NUMERO_TARJETA,
                'TBK_FECHA_CONTABLE, 'TBK_FECHA_TRANSACCION, 'TBK_HORA_TRANSACCION, 'TBK_ID_TRANSACCION, 'TBK_CODIGO_AUTORIZACION,
                'TBK_TIPO_PAGO, 'TBK_NUMERO_CUOTAS, 'TBK_VCI, 'TBK_MAC).as(KccConfirmation) { kccConfirmation =>
                Route { ctx =>
                  Transactions.findByOrderNumberSessionIdAndStatus(kccConfirmation.orderNumber, kccConfirmation.sessionId, Transactions.IN_PROGRESS).flatMap {
                    case Some(transaction) => {
                      Transactions.setTbkTransactionDataAndStatus(transaction.id.get, TbkTransactionData(kccConfirmation), Transactions.CONFIRMED).flatMap { t =>

                        //Validamos monto
                        val amountOk = transaction.purchaseAmount == kccConfirmation.purchaseAmount.toDouble / 100

                        //Validamos respuesta de transbank
                        val responseOk:Boolean = kccConfirmation.response.toInt == 0

                        //Escribímos archivo para verificación de mac
                        val checkMacData = s"TBK_ORDEN_COMPRA=${kccConfirmation.orderNumber}&" +
                          s"TBK_TIPO_TRANSACCION=${kccConfirmation.transactionType}&" +
                          s"TBK_RESPUESTA=${kccConfirmation.response}&" +
                          s"TBK_MONTO=${kccConfirmation.purchaseAmount}&" +
                          s"TBK_CODIGO_AUTORIZACION=${kccConfirmation.authorizationCode}&" +
                          s"TBK_FINAL_NUMERO_TARJETA=${kccConfirmation.cardFinalNumbers}&" +
                          s"TBK_FECHA_CONTABLE=${kccConfirmation.accountingDate}&" +
                          s"TBK_FECHA_TRANSACCION=${kccConfirmation.txDate}&" +
                          s"TBK_HORA_TRANSACCION=${kccConfirmation.txTime}&" +
                          s"TBK_ID_SESION=${kccConfirmation.sessionId}&" +
                          s"TBK_ID_TRANSACCION=${kccConfirmation.txId}&" +
                          s"TBK_TIPO_PAGO=${kccConfirmation.paymentType}&" +
                          s"TBK_NUMERO_CUOTAS=${kccConfirmation.installments}&" +
                          s"TBK_VCI=${kccConfirmation.vci}&" +
                          s"TBK_MAC=${kccConfirmation.mac}"
                        val time = System.currentTimeMillis()
                        new PrintWriter(s"/tmp/tbk-${time}.txt") {
                          write(checkMacData); close
                        }
                        val checkMacCmd = transaction.currencyId match {
                          case "CLP" => s"$cgiBaseDir/cgi-bin/clp/tbk_check_mac.cgi"
                          case "USD" => s"$cgiBaseDir/cgi-bin/clp/tbk_check_mac.cgi"
                          case _ => "cgi-bin"
                        }
                        val resultCheckMac = s"$checkMacCmd /tmp/tbk-${time}.txt" !!
                        val checkMac = resultCheckMac.trim == "CORRECTO"

                        // Si el monto, respuesta y mac están ok respondemos ACEPTADO
                        if (amountOk && responseOk && checkMac) {
                          Transactions.updateStatus(transaction.id.get, Transactions.ACCEPTED).flatMap { t =>
                            ctx.complete("ACEPTADO")
                          }
                        } else if (!responseOk) {
                          /**
                            * Las pruebas realizadas con la tarjeta Mastercard de pruebas siempre conducirán a un rechazo
                            * de la transacción a nivel bancario, representado en la data por un TBK_RESPUESTA distinto
                            * de cero (-8 a -1, en las distintas respuestas que pueden entregarse). Este rechazo, según
                            * el flujo explicado en la página 37 del Manual de Integración, debe responderse SIEMPRE
                            * “ACEPTADO” en mayúscula, lo cual deriva en página de fracaso (en el archivo tbk_bitacora
                            * se genera ACK y esto confirma que fue aceptada la transacción).
                            */
                          Transactions.updateStatus(transaction.id.get, Transactions.REJECTED).flatMap { t =>
                            ctx.complete("ACEPTADO")
                          }
                        } else {
                          Transactions.updateStatus(transaction.id.get, Transactions.REJECTED).flatMap { t =>
                            ctx.complete("RECHAZADO")
                          }
                        }
                      }
                    }
                    case None =>
                      //La orden ya fue procesada
                      Transactions.findByOrderNumber(kccConfirmation.orderNumber).flatMap { transaction =>
                        Transactions.updateStatus(transaction.get.id.get, Transactions.REJECTED).flatMap { t =>
                          ctx.complete("RECHAZADO")
                        }
                      }
                  }
                }
              }
            } ~
            path("success") {
              formFields('TBK_ORDEN_COMPRA, 'TBK_ID_SESION) { (orderNumber, sessionId) =>
                Route { ctx =>
                  Transactions.findByOrderNumberSessionIdAndStatus(orderNumber, sessionId, Transactions.ACCEPTED).flatMap {
                    case Some(transaction) => {
                      ctx.complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Payment.commerceSuccess(transaction)))
                    }
                    case None => {
                      println(s"FATAL. Transaction not found sessionId: $sessionId, $orderNumber")
                      ctx.fail(new Exception("Payment approved, but order not found in local server"))
                    }
                  }
                }
              }
            } ~ path("failure") {
            formFields('TBK_ORDEN_COMPRA, 'TBK_ID_SESION) { (orderNumber, sessionId) =>
              Route { ctx =>
                Transactions.findByOrderNumberSessionIdAndStatus(orderNumber, sessionId, Transactions.REJECTED).flatMap {
                  case Some(transaction) => {
                    ctx.complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Payment.commerceFailure(transaction)))
                  }
                  case None => {
                    println(s"FATAL. Transaction not found sessionId: $sessionId, $orderNumber")
                    ctx.fail(new Exception("Payment rejected, but order not found in local server"))
                  }
                }
              }
            }
          }
        }
      }

  val viewsRoute = get {
    path("payment1") {
      complete(
        HttpEntity(ContentTypes.`text/html(UTF-8)`, Payment.paymentForm(kccAddress, kccPort))
      )
    } ~
      pathPrefix("test") {
        path("failure") {
          complete(
            HttpEntity(ContentTypes.`text/html(UTF-8)`, Payment.testFailure1)
          )
        }
      }
  } ~ post {
    pathPrefix("test") {
      path("success") {
        formFields('orderNumber, 'sessionId) { (orderNumber, sessionId) =>
          Route { ctx =>
            Transactions.findByOrderNumberSessionIdAndStatus(orderNumber, sessionId, Transactions.ACCEPTED).flatMap {
              case Some(transaction) => ctx.complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Payment.testSuccess(transaction)))
              case None => ctx.fail(new Exception("Order not found in local server"))
            }
          }
        }
      } ~
        path("failure") {
          formFields('orderNumber.?, 'sessionId.?) { (orderNumberOpt, sessionId) =>
            orderNumberOpt match {
              case Some(orderNumber) => Route { ctx =>
                Transactions.findByOrderNumberSessionIdAndStatus(orderNumber, sessionId.get, Transactions.REJECTED).flatMap {
                  case Some(transaction) => ctx.complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Payment.testFailure2(transaction)))
                  case None => ctx.complete(
                    HttpEntity(ContentTypes.`text/html(UTF-8)`, Payment.testFailure1)
                  )
                }
              }
              case _ => complete(
                HttpEntity(ContentTypes.`text/html(UTF-8)`, Payment.testFailure1)
              )
            }
          }
        }
    }
  }
  /*val documentation = get {
    pathPrefix("documentation") {
      encodeResponse {
        getFromResourceDirectory("documentation")
      }
    }
  }*/
  val adminRoute = get {
    pathPrefix("admin") {
      path("create-tables") {
        complete(
          Users.createTable map (i => "OK") //Future.sequence(Seq(Transactions.createTable, Commerces.createTable)) map (i=>"OK")
        )
      }
    }
  }
  val proxyRoute = pathPrefix("cgi-bin") {
    CGIDirectives.buildEnv("/cgi-bin/tbk_bp_pago.cgi") { env =>
      Route {
        context =>
          val request = context.request


          //cgiRunner.run("/cgi-bin/tbk_bp_pago.cgi", request, Map(("a"->"a")))

          println("Proxy to: " + request.uri)
          val flow = Http(system).outgoingConnection(kccAddress, kccPort)
          val handler = Source.single(context.request)
            .map(r => r.withHeaders(RawHeader("x-authenticated", "someone"), Host(kccAddress, kccPort)))
            .via(flow)
            .runWith(Sink.head)
            .flatMap(context.complete(_))
          handler
      }
    }

  }
  val routes =
    viewsRoute ~ paymentRoute ~ proxyRoute ~ AdminRoutes.routes

  def main(args: Array[String]) {

    val bindingFuture = Http().bindAndHandle(Boot.routes, serverAddress, serverPort)

    println(s"Server online at http://$serverAddress:$serverPort/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}

object Util {
  def redirectMessage(uri: String, message: String) = {
    val redirectionType = StatusCodes.TemporaryRedirect
    HttpResponse(
      status = redirectionType,
      headers = headers.Location(uri) :: Nil,
      entity = redirectionType.htmlTemplate match {
        case "" ⇒ HttpEntity.Empty
        case template ⇒ HttpEntity(ContentTypes.`text/html(UTF-8)`, template format uri)
      })
  }
}
