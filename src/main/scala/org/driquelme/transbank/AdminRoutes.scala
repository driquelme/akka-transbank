package org.driquelme.transbank

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.util.Tuple
import akka.http.scaladsl.settings.RoutingSettings
import org.driquelme.transbank.Data.{Users, Commerce, User, Transactions}
import views.Layout

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by danielriquelme on 23-04-16.
 */
object AdminRoutes {

  import Config._

  def auth(implicit ec: ExecutionContext): Directive[(User, Commerce)] = {
    authenticateBasicAsync(realm = "Payment Gateway", authenticator).flatMap { user =>
      onSuccess(Data.Commerces.findById(user.commerceId)).flatMap {
        commerce => tprovide((user, commerce))
      }
    }
  }

  /*def userCommerce(email: String)(implicit ec: ExecutionContext, ev: Tuple[(User, Commerce)]) = new Directive1[(User, Commerce)]  {
    type Out = (User, Commerce)
    def tapply(f: Out ⇒ Route) {
      val tp = for {
        user <- Data.Users.findByEmail(email)
        commerce <- Data.Commerces.findById(user.id.get)
      } yield (user, commerce)
      tp.foreach(t => f(t._1, t._2))
    }
  }*/

  def authenticator(credentials: Credentials)(implicit ec: ExecutionContext): Future[Option[User]] =
    credentials match {
      case p@Credentials.Provided(email) =>
        Data.Users.authenticate(email, p.verify)
      case _ => Future.successful(None)
    }

  def views(implicit routingSettings: RoutingSettings, ec: ExecutionContext) = Route.seal {
    pathPrefix("admin") {
      auth(ec) { (user: User, commerce: Commerce) =>
        path("index") {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Layout.index.render))
        } ~
          path("documentation") {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Layout.documentation(kccAddress, kccPort).render))
          } ~
          path("transaction-log") {
            complete {
              Transactions.all.map { transactions =>
                HttpEntity(ContentTypes.`text/html(UTF-8)`, Layout.transactionLog(transactions).render)
              }
            }
          }
      }
    }
  }


  def routes(implicit routingSettings: RoutingSettings, ec: ExecutionContext) = views
}
