package org.driquelme.transbank

/**
  * Created by danielriquelme on 19-04-16.
  */

import org.driquelme.transbank.Boot.KccConfirmation
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

import scala.concurrent.ExecutionContext

object Data {
  val db = Database.forConfig("akka-transbank.db")

  case class Transaction(
                          id: Option[Int],
                          status: String,
                          timestamp: java.sql.Timestamp,
                          sessionId: String,
                          orderNumber: String,
                          purchaseAmount: Double,
                          successUrl: String,
                          failureUrl: String,
                          confirmationUrl: String,
                          acknowledgeURL: String,
                          userDisplayName: Option[String],
                          purchaseDescription: Option[String],
                          currencyId: String,
                          tbkTransactionData: Option[TbkTransactionData] = None)

  val Separator = ":-:"
  case class TbkTransactionData(tbkTransactionType: String, tbkResponse: String, tbkAmount: String,
                                tbkCardFinalNumbers: String, tbkAccountingDate: String,
                                tbkTxDate: String, tbkTxTime: String, tbkTxId: String,
                                tbkAuthorizationCode: String, tbkPaymentType: String,
                                tbkInstallments: String, tbkVci: String, tbkMac: String) {
    override def toString = {
      s"$tbkTransactionType$Separator$tbkResponse$Separator$tbkAmount$Separator$tbkCardFinalNumbers$Separator" +
        s"$tbkAccountingDate$Separator$tbkTxDate$Separator$tbkTxTime$Separator$tbkTxId$Separator" +
        s"$tbkAuthorizationCode$Separator$tbkPaymentType$Separator$tbkInstallments$Separator$tbkVci$Separator" +
        s"$tbkMac$Separator"
    }
  }
  object TbkTransactionData {
    def apply(str: String) = {
      val fields = str.split(Separator)
      new TbkTransactionData(fields(0), fields(1), fields(2), fields(3), fields(4), fields(5), fields(6), fields(7),
        fields(8), fields(9), fields(10), fields(11), fields(12))
    }
    def apply(kf: KccConfirmation) = {
      new TbkTransactionData(kf.transactionType, kf.response, kf.purchaseAmount, kf.cardFinalNumbers, kf.accountingDate,
      kf.txDate, kf.txTime, kf.txId, kf.authorizationCode, kf.paymentType, kf.installments, kf.vci, kf.mac)
    }
  }

  object Transaction {
    def fromPaymentInfo(paymentInfo: Boot.PaymentInfo): Transaction =
      Transaction(None, Transactions.IN_PROGRESS, new java.sql.Timestamp(System.currentTimeMillis()),
        paymentInfo.sessionId, paymentInfo.orderNumber, paymentInfo.purchaseAmount, paymentInfo.successUrl,
        paymentInfo.failureUrl, paymentInfo.confirmationURL, paymentInfo.acknowledgeURL,
        paymentInfo.userDisplayName,
        paymentInfo.purchaseDescription, paymentInfo.currencyId)
  }
  implicit def tbkTransactionDataColumnType = MappedColumnType.base[Option[TbkTransactionData], String](
    tbkTransactionData => tbkTransactionData.map(_.toString).getOrElse(""),
    str => if (str.trim.length>0) {
      Some(TbkTransactionData(str))
    } else {
      None
    }
  )
  class Transactions(tag: Tag) extends Table[Transaction](tag, "transactions") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def status = column[String]("status")
    def timestamp = column[java.sql.Timestamp]("timestamp")
    def sessionId = column[String]("session_id")
    def orderNumber = column[String]("order_number")
    def purchaseAmount = column[Double]("purchase_amount")
    def successUrl = column[String]("success_url")
    def failureUrl = column[String]("failure_url")
    def confirmationUrl = column[String]("confirmation_url")
    def acknowledgeURL = column[String]("acknowledge_url")
    def userDisplayName = column[Option[String]]("user_display_name")
    def purchaseDescription = column[Option[String]]("purchase_description")
    def currencyId = column[String]("currency_id")
    def tbkTransactionData = column[Option[TbkTransactionData]]("tbk_transaction_data", O.SqlType("TEXT"))

    def * = (id.?, status, timestamp, sessionId, orderNumber, purchaseAmount, successUrl, failureUrl, confirmationUrl,
      acknowledgeURL, userDisplayName, purchaseDescription, currencyId, tbkTransactionData) <>
      ((Transaction.apply _).tupled, Transaction.unapply)
  }
  object Transactions {
    val transactions = TableQuery[Transactions]

    val IN_PROGRESS = "IN_PROGRESS"   //Transaction started
    val APPROVED = "APPROVED"         //Transaction approved by Transbank
    val CONFIRMED = "CONFIRMED"       //Transaction approved by Transbank and confirmed by commerce
    val ACCEPTED = "ACCEPTED"         //Transaction approved by Transbank, confirmed by commerce and confirmed accepted by transbank
    val REJECTED = "REJECTED"         //Transaction rejected
    val ERROR = "ERROR"               //Error during transaction

    def createTable = db run( DBIO.seq( transactions.schema.create ) )
    def all = db run( transactions sortBy(_.timestamp.desc)  result )
    def findByOrderNumber(orderNumber: String) = db run( transactions.filter(_.orderNumber === orderNumber).result.headOption)
    def findByOrderNumberAndStatus(orderNumber: String, status: String) = db run (
      transactions.filter(t => t.orderNumber === orderNumber && t.status === status).result.headOption
      )
    def findByOrderNumberSessionIdAndStatus(orderNumber: String, sessionId: String, status: String) = db run (
      transactions.filter(t => t.orderNumber === orderNumber && t.sessionId === sessionId && t.status === status).result.headOption
      )
    def insert(transaction: Transaction) = db run (transactions += transaction)
    def setTbkTransactionDataAndStatus(id: Int, tbkTransactionData: TbkTransactionData, status: String) = db run (
      transactions.filter(_.id === id).map(t => (t.tbkTransactionData, t.status)).update((Some(tbkTransactionData), status))
      )
    def updateStatus(id: Int, status: String) = db run ( transactions.filter(_.id === id).map (_.status).update (status))
  }

  case class Commerce(
                     id: Option[Int],
                     name: String,
                     key: String,
                     secret: String
                       )
  class Commerces(tag: Tag) extends Table[Commerce](tag, "commerces") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def key = column[String]("key")
    def secret = column[String]("secret")
    def * = (id.?, name, key, secret) <> ((Commerce.apply _).tupled, Commerce.unapply)
  }
  object Commerces {
    val commerces = TableQuery[Commerces]
    def createTable = db run( DBIO.seq( commerces.schema.create ) )
    def all = db run( commerces result )
    def findById(id: Int) = db run ( commerces.filter(_.id === id).result.head)
    def insert(commerce: Commerce) = db run (commerces += commerce)
    def authenticate(key: String, verify: String => Boolean)(implicit ec: ExecutionContext) = db run ( commerces
      .filter ( c => c.key === key)
      .result
      .map { set =>
        set.map (_.secret)
          .filter(verify)
          .size > 0
      }
    )
  }

  case class User(
                 id: Option[Int],
                 name: String,
                 email: String,
                 password: String,
                 commerceId: Int)
  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def email = column[String]("email")
    def password = column[String]("password")
    def commerceId = column[Int]("commerce_id")
    def * = (id.?, name, email, password, commerceId) <> ((User.apply _).tupled, User.unapply)
  }
  object Users {
    val users = TableQuery[Users]
    def createTable = db run ( DBIO.seq( users.schema.create ))
    def findByEmail(email: String) = db run ( users.filter((_.email === email)).result.head  )
    def authenticate(email: String, verify: String => Boolean)(implicit ec: ExecutionContext) = db run ( users
      .filter ( _.email === email)
      .result
      .headOption
      .map { opt =>
        opt match {
          case Some(user) =>
            if( verify(user.password) ) Some(user)
            else None
          case _ => None
        }
      }
    )
  }
}
