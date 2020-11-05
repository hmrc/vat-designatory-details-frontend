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

package controllers.tradingName

import audit.AuditingService
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.TradingNameForm.tradingNameForm
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.VatSubscriptionService
import views.html.tradingName.CaptureTradingNameView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureTradingNameController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                             val errorHandler: ErrorHandler,
                                             val auditService: AuditingService,
                                             captureTradingNameView: CaptureTradingNameView)
                                            (implicit val appConfig: AppConfig,
                                             mcc: MessagesControllerComponents,
                                             authComps: AuthPredicateComponents,
                                             inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>
      val validationTradingName: Future[Option[String]] = user.session.get(SessionKeys.validationTradingNameKey) match {
        case Some(tradingName) => Future.successful(Some(tradingName))
        case _ =>
          vatSubscriptionService.getCustomerInfo(user.vrn) map {
            case Right(details) => Some(details.tradingName.getOrElse(""))
            case _ => None
          }
      }

      val prepopulationTradingName: Future[String] = validationTradingName map { validation =>
        user.session.get(SessionKeys.prepopulationTradingNameKey)
          .getOrElse(validation.getOrElse(""))
      }

      for {
        validation <- validationTradingName
        prepopulation <- prepopulationTradingName
      } yield {
        validation match {
          case Some(valTradingName) =>
            Ok(captureTradingNameView(tradingNameForm(valTradingName).fill(prepopulation), valTradingName))
              .addingToSession(SessionKeys.validationTradingNameKey -> valTradingName)
          case _ => errorHandler.showInternalServerError
        }
      }
  }

  def submit: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>
    val validationTradingName: Option[String] = user.session.get(SessionKeys.validationTradingNameKey)

    validationTradingName match {
      case Some(validation) => tradingNameForm(validation).bindFromRequest.fold(
        errorForm => {
          Future.successful(BadRequest(captureTradingNameView(errorForm, validation)))
        },
        tradingName => {
          Future.successful(Redirect(routes.CheckYourAnswersController.show())
            .addingToSession(SessionKeys.prepopulationTradingNameKey -> tradingName))
        }
      )
      case None => Future.successful(errorHandler.showInternalServerError)
    }
  }
}
