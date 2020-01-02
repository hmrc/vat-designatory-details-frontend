/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.predicates.inflight

import common.SessionKeys.inFlightTradingNameChangeKey
import config.AppConfig
import models.User
import models.customerInformation.CustomerInformation
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.{Conflict, Redirect}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.LoggerUtil.{logDebug, logWarn}

import scala.concurrent.{ExecutionContext, Future}

class InFlightPredicate(inFlightComps: InFlightPredicateComponents,
                        redirectURL: String) extends ActionRefiner[User, User] with I18nSupport {

  implicit val appConfig: AppConfig = inFlightComps.appConfig
  implicit val executionContext: ExecutionContext = inFlightComps.ec
  implicit val messagesApi: MessagesApi = inFlightComps.messagesApi

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val req: User[A] = request

    req.session.get(inFlightTradingNameChangeKey) match {
      case Some("false") => Future.successful(Right(req))
      case Some(change) => Future.successful(Left(Conflict(inFlightComps.inFlightChangeView(change))))
      case None => getCustomerInfoCall(req.vrn)
    }
  }

  private def getCustomerInfoCall[A](vrn: String)
                                    (implicit hc: HeaderCarrier, request: User[A]): Future[Either[Result, User[A]]] =
    inFlightComps.vatSubscriptionService.getCustomerInfo(vrn).map {
      case Right(customerInfo) =>
        customerInfo.pendingChanges match {
          case Some(changes) if changes.tradingName.isDefined =>
            comparePendingAndCurrent(customerInfo)
          case _ =>
            logDebug("[InFlightPredicate][getCustomerInfoCall] - There are no in-flight changes. " +
              "Redirecting user to the start of the journey.")
            Left(Redirect(redirectURL).addingToSession(inFlightTradingNameChangeKey -> "false"))
        }
      case Left(error) =>
        logWarn("[InFlightPredicate][getCustomerInfoCall] - " +
          s"The call to the GetCustomerInfo API failed. Error: ${error.message}")
        Left(inFlightComps.errorHandler.showInternalServerError)
    }

  private def comparePendingAndCurrent[A](customerInfo: CustomerInformation)
                                         (implicit user: User[A]): Either[Result, User[A]] = {

    def logWarnPending(changeType: String): Unit = logWarn("[InFlightPredicate][comparePendingAndCurrent] - " +
      s"There is an in-flight $changeType change. Rendering graceful error page.")

    if(!customerInfo.sameTradingName) {
      logWarnPending("trading name")
      Left(Conflict(inFlightComps.inFlightChangeView("trading name"))
        .addingToSession(inFlightTradingNameChangeKey -> "tradingName"))
    } else {
      Right(user)
    }
  }
}
