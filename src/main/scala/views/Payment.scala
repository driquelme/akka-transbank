package views

import org.driquelme.transbank.Boot.PaymentInfo
import org.driquelme.transbank.Data.Transaction

import scalatags.Text.all._

/**
 * Created by danielriquelme on 21-04-16.
 */
object Payment {

  import Layout._

  def paymentForm(server: String, port: Int) = layout(testFormTitle) {
    div(

      form(method := "post", action := "payment/start", name := "paymentForm")(
        div(`class` := "form-group")(
          label("for".attr := "sessionId")("sessionId"),
          input(`class` := "form-control")(name := "sessionId", value := "1")
        ),
        div(`class` := "form-group")(
          label("for".attr := "key")("Commerce Key"),
          input(`class` := "form-control")(name := "key", value := "LAW")
        ),
        div(`class` := "form-group")(
          label("for".attr := "secret")("Commerce Secret"),
          input(`class` := "form-control")(name := "secret", value := "e189252abd9160ef6acce3f6e2e6e959")
        ),
        div(`class` := "form-group")(
          label("for".attr := "orderNumber")("Order Number"),
          input(`class` := "form-control")(name := "orderNumber", value := "1231231")
        ),
        div(`class` := "form-group")(
          label("for".attr := "purchaseAmount")("Purchase Amount"),
          input(`class` := "form-control")(name := "purchaseAmount", value := "10000000")
        ),
        div(`class` := "form-group")(
          label("for".attr := "successUrl")("Success URL"),
          input(`class` := "form-control")(name := "successUrl", value := s"https://$server:$port/api/test/success")
        ),
        div(`class` := "form-group")(
          label("for".attr := "failureUrl")("Failure URL"),
          input(`class` := "form-control")(name := "failureUrl", value := s"https://$server:$port/api/test/failure")
        ),
        div(`class` := "form-group")(
          label("for".attr := "confirmationURL")("confirmation URL"),
          input(`class` := "form-control")(name := "confirmationURL", value := s"https://$server:$port/api/test/confirmation")
        ),
        div(`class` := "form-group")(
          label("for".attr := "acknowledgeURL")("acknowledge URL"),
          input(`class` := "form-control")(name := "acknowledgeURL", value := s"https://$server:$port/api/test/acknowledge")
        ),
        div(`class` := "form-group")(
          label("for".attr := "userDisplayName")("User Display Name"),
          input(`class` := "form-control")(name := "userDisplayName", value := "Robert Jones")
        ),
        div(`class` := "form-group")(
          label("for".attr := "purchaseDescription")("Purchase Description"),
          input(`class` := "form-control")(name := "purchaseDescription", value := "Miami Weekend")
        ),
        div(`class` := "form-group")(
          label("for".attr := "currencyId")("Currency"),
          input(`class` := "form-control")(name := "currencyId", value := "CLP")
        ),
        button(`type` := "submit", value := "Submit")("Submit")
      )
    )
  }.render

  def testSuccess(transaction: Transaction) = layout("Successful Payment") {
    div(
      table( `class` := "table table-bordered" )(
        tr(th("Order Number"), th("Session Id"), th("Authorization Code")),
        tr(td(transaction.orderNumber), td(transaction.sessionId), td(transaction.tbkTransactionData.get.tbkAuthorizationCode))
      )
    )
  }.render
  def testFailure1 = layout("Failure") {
    div(
      p("Please check you are not trying to pay an already payed order number.")
    )
  }.render
  def testFailure2(transaction: Transaction) = layout("Failed Payment") {
    div(
      table( `class` := "table table-bordered" )(
        tr(th("Order Number"), th("Session Id")),
        tr(td(transaction.orderNumber), td(transaction.sessionId))
      )
    )
  }.render

  def kccPaymentForm(server: String, cgi: String, port: Int, paymentInfo: PaymentInfo) = {
    div(
      h1("Payment Redirection"),
      body(onload := "if (document.paymentForm != null) document.paymentForm.submit();")(
        form(method := "post", action := s"https://$server:$port/$cgi/tbk_bp_pago.cgi", name := "paymentForm")(
          input(name := "TBK_TIPO_TRANSACCION", value := "TR_NORMAL", `type` := "hidden"),
          input(name := "TBK_MONTO", value := paymentInfo.purchaseAmount.toInt, `type` := "hidden"),
          input(name := "TBK_ORDEN_COMPRA", value := paymentInfo.orderNumber, `type` := "hidden"),
          input(name := "TBK_ID_SESION", value := paymentInfo.sessionId, `type` := "hidden"),
          input(name := "TBK_URL_EXITO", value := s"https://$server:$port/api/payment/success", `type` := "hidden"),
          input(name := "TBK_URL_FRACASO", value := s"https://$server:$port/api/payment/failure", `type` := "hidden")
        )
      )
    )
  }.render

  def commerceSuccess(transaction: Transaction) = {
    div(
      h1("Payment Redirection: Success"),
      body(onload := "if (document.paymentForm != null) document.paymentForm.submit();")(
        form(method := "post", action := s"${transaction.successUrl}", name := "paymentForm")(
          input(name := "orderNumber", value := transaction.orderNumber, `type` := "hidden"),
          input(name := "sessionId", value := transaction.sessionId, `type` := "hidden"),
          input(name := "authorizationCode", value := transaction.tbkTransactionData.get.tbkAuthorizationCode, `type` := "hidden"),
          input(name := "cardFinalNumbers", value := transaction.tbkTransactionData.get.tbkCardFinalNumbers, `type` := "hidden"),
          input(name := "accountingDate", value := transaction.tbkTransactionData.get.tbkAccountingDate, `type` := "hidden"),
          input(name := "txDate", value := transaction.tbkTransactionData.get.tbkTxDate, `type` := "hidden"),
          input(name := "time", value := transaction.tbkTransactionData.get.tbkTxTime, `type` := "hidden"),
          input(name := "txId", value := transaction.tbkTransactionData.get.tbkTxId, `type` := "hidden"),
          input(name := "installments", value := transaction.tbkTransactionData.get.tbkInstallments, `type` := "hidden"),
          input(name := "paymentType", value := transaction.tbkTransactionData.get.tbkPaymentType, `type` := "hidden"),
          input(name := "transactionType", value := transaction.tbkTransactionData.get.tbkTransactionType, `type` := "hidden"),
          input(name := "installmentType", value := transaction.tbkTransactionData.get.tbkPaymentType, `type` := "hidden")
        )
      )
    )
  }.render

  def commerceFailure(transaction: Transaction) = {
    div(
      h1("Payment Redirection: Failure"),
      body(onload := "if (document.paymentForm != null) document.paymentForm.submit();")(
        form(method := "post", action := s"${transaction.failureUrl}", name := "paymentForm")(
          input(name := "orderNumber", value := transaction.orderNumber, `type` := "hidden"),
          input(name := "sessionId", value := transaction.sessionId, `type` := "hidden")
        )
      )
    )
  }.render
}
