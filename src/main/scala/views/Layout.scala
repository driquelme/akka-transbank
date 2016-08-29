package views

import org.driquelme.transbank.Data.Transaction

import scalatags.Text.TypedTag
import scalatags.Text.all._

/**
 * Created by danielriquelme on 23-04-16.
 */
object Layout {

  val testFormTitle = "Test Form"

  val formatter = java.text.NumberFormat.getIntegerInstance

  def index = layout("Welcome")(
    h2("Welcome to the Payment Gateway admin page.")
  )

  def documentation(server: String, port: Int) = layout("Documentation")(
    div(
      h2("Test Form"),
      p("Use the test form to initiate a test transaction from this site."),
      p("Use the same card numbers and credentials to test from the Commerce site."),
      ul(
        li(b("Successful card number:"), " 4051885600446623"),
        li(b("Failure card number:"), " 5186059559590568"),
        li(b("Verification Code:"), " 123"),
        li(b("Card date:"), " Any date"),
        li(b("RUT:"), " 11.111.111-1"),
        li(b("Clave:"), " 123")
      ),
      hr,
      h2("API"),
      h3("Start Payment Process"),
      i(s"POST https://$server:$port/api/payment/start"),
      br, br,
      table(`class` := "table")(
        tr(th("Parameter"), th("Description")),
        tr(td("sessionId"), td("Session identifier generated by Commerce system")),
        tr(td("orderNumber"), td("The order identifier visible to the customer")),
        tr(td("purchaseAmount"), td("Total order amount")),
        tr(td("successUrl"), td("At the end of the process the Law Webpay Server will redirect to this url if payment is successful")),
        tr(td("failureUrl"), td("At the end of the process the Law Webpay Server will redirect to this url if payment is not successful")),
        tr(td("confirmationURL"), td("Url used by Law Webpay Server to request Ezy for payment confirmation")),
        tr(td("userDisplayName"), td("User name and lastname, this information is used by Law Webpay Server to render the payment result to the user")),
        tr(td("purchaseDescription"), td("Description of the items purchased")),
        tr(td("key"), td("Commerce Key")),
        tr(td("secret"), td("Commerce Secret"))
      ),
      hr,
      h2("Callbacks"),
      h3("Success URL"),
      p("The Payment Gateway will make a POST request to this url upon payment success."),
      p("The parameters are:"),
      table(`class` := "table")(
        tr(th("Parameter"), th("Description")),
        tr(td("orderNumber"), td("Order Number")),
        tr(td("sessionId"), td("Session Id")),
        tr(td("authorizationCode"), td("Authorization Code (generated by Transbank)")),
        tr(td("cardFinalNumbers"), td("Card Number final four numbers (generated by Transbank)")),
        tr(td("accountingDate"), td("Accounting Date (generated by Transbank)")),
        tr(td("txDate"), td("Transaction Date (generated by Transbank)")),
        tr(td("time"), td("Transaction Time (generated by Transbank)")),
        tr(td("txId"), td("Transaction Id (generated by Transbank)")),
        tr(td("installments"), td("Installments (generated by Transbank)"))
      ),
      h3("Failure URL"),
      p("The Payment Gateway will make a POST request to this url upon payment failure."),
      p("The parameters are:"),
      table(`class` := "table")(
        tr(th("Parameter"), th("Description")),
        tr(td("orderNumber"), td("Order Number")),
        tr(td("sessionId"), td("Session Id"))
      )
    )
  )

  def transactionLog(transactions: Seq[Transaction]) = layout("Transaction Log")(
    table(`class` := "table")(
      tr(
        th("Id"), th("Status"), th("Timestamp"), th("Session Id"), th("Order Number"), th("Purchase Amount"),
        th("Currency"), th("TBK Card Final Numbers"), th("TBK Authorization Code"), th("TBK Tx Date"), th("TBK Tx Time"),
        th("TBK Payment Type"), th("TBK Installments"), th("TBK Accounting Date"), th("TBK Response")
      ),
      transactions.map { transaction =>
        tr(
          td(transaction.id), td(transaction.status), td(transaction.timestamp.toString), td(transaction.sessionId),
          td(transaction.orderNumber), td(formatter.format(transaction.purchaseAmount)),
          td(transaction.currencyId),
          td(transaction.tbkTransactionData.map(_.tbkCardFinalNumbers)),
          td(transaction.tbkTransactionData.map(_.tbkAuthorizationCode)),
            td(transaction.tbkTransactionData.map(_.tbkTxDate)),
          td(transaction.tbkTransactionData.map(_.tbkTxTime)),
          td(transaction.tbkTransactionData.map(_.tbkPaymentType)),
          td(transaction.tbkTransactionData.map(_.tbkInstallments)),
          td(transaction.tbkTransactionData.map(_.tbkAccountingDate)),
          td(transaction.tbkTransactionData.map(_.tbkResponse))
        )
      }
    )
  )

  def navbar = nav(`class` := "navbar navbar-default navbar-fixed-top")(
    div(`class` := "container")(
      div(`class` := "navbar-header")(
        button(`type` := "button", `class` := "navbar-toggle collapsed", "data-toggle".attr := "collapse",
          "data-target".attr := "#navbar", "aria-expanded".attr := "false", "aria-controls".attr := "navbar"
        )(
            span(`class` := "sr-only")("Toggle Navigation"),
            span(`class` := "icon-bar"),
            span(`class` := "icon-bar"),
            span(`class` := "icon-bar")
          ),
        a(`class` := "navbar-brand", href := "#")("Payment Gateway")
      ),
      div(id := "navbar", `class` := "collapse navbar-collapse")(
        ul(`class` := "nav navbar-nav")(
          li(a(href := "/api/admin/index")("Home")),
          li(a(href := "/api/admin/documentation")("Documentation")),
          li(a(href := "/api/admin/transaction-log")("Transaction Log")),
          li(a(href := "/api/payment1")("Test Form"))
        )
      )
    )
  )

  def layout(title: String)(content: TypedTag[String]) = html {
    head(
      link(rel := "stylesheet", href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"),
      link(rel := "stylesheet", href := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css"),
      script(src := "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js")
    )(
        body(
          navbar,
          div(`class` := "container", style := "padding: 60px 15px 0;")(
            div(`class` := "page-header")(
              h1(title)
            ),
            content
          )
        )
      )
  }

  lazy val nav = "nav".tag[String]
  lazy val headStyle = "style".tag[String]

  def stickyFooterStyle =
    """
      |/* Sticky footer styles
      |-------------------------------------------------- */
      |html {
      |  position: relative;
      |  min-height: 100%;
      |}
      |body {
      |  /* Margin bottom by footer height */
      |  margin-bottom: 60px;
      |}
      |.footer {
      |  position: absolute;
      |  bottom: 0;
      |  width: 100%;
      |  /* Set the fixed height of the footer here */
      |  height: 60px;
      |  background-color: #f5f5f5;
      |}
      |
      |
      |/* Custom page CSS
      |-------------------------------------------------- */
      |/* Not required for template or sticky footer method. */
      |
      |body > .container {
      |  padding: 60px 15px 0;
      |}
      |.container .text-muted {
      |  margin: 20px 0;
      |}
      |
      |.footer > .container {
      |  padding-right: 15px;
      |  padding-left: 15px;
      |}
      |
      |code {
      |  font-size: 80%;
      |}
      |
      |
    """.stripMargin
}
