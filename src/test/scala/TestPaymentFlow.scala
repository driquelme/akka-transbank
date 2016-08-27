import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.driquelme.transbank.Boot
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by danielriquelme on 19-04-16.
  */
class TestPaymentFlow extends WordSpec with Matchers with ScalatestRouteTest{

  val query1 = Seq("token=1", "sessionId=20160419094930","orderNumber=20160419094930","purchaseAmount=1000000","successUrl=surl","failureUrl=sdf",
    "confirmationURL=asdf", "userDisplayName=jhg").mkString("&")

  "The Payment server" should {
    /*"Handle a test request" in {
      Get("/test") ~> Boot.paymentRoute ~> check {
        responseAs[String] shouldEqual "ok"
      }
    }*/
    /*"Handle an incoming ecommerce payment redirection" in {
      Get("/payment/start?" + query1) ~> Boot.paymentRoute ~> check {
        responseAs[String] shouldEqual "A"
      }
    }*/
  }
}
