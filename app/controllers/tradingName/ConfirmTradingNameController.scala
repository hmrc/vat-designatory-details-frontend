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
import audit.models.ChangedTradingNameAuditModel
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import controllers.BaseController
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.errors.ErrorModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil.logWarn
import views.html.tradingName.ConfirmTradingNameView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmTradingNameController @Inject()(val errorHandler: ErrorHandler,
                                         val vatSubscriptionService: VatSubscriptionService,
                                         confirmTradingNameView: ConfirmTradingNameView,
                                         auditService: AuditingService)
                                        (implicit val appConfig: AppConfig,
                                         mcc: MessagesControllerComponents,
                                         authComps: AuthPredicateComponents,
                                         inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>

    user.session.get(prepopulationTradingNameKey).filter(_.nonEmpty) match {
      case Some(tradingName) =>
        Ok(confirmTradingNameView(tradingName))
      case _ =>
        Redirect(routes.CaptureTradingNameController.show())
    }
  }

  def updateTradingName(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>

    user.session.get(prepopulationTradingNameKey) match {
      case Some(tradingName) =>
        vatSubscriptionService.updateTradingName(user.vrn, tradingName) map {
          case Right(_) =>
            auditService.audit(
              ChangedTradingNameAuditModel(
                user.session.get(validationTradingNameKey),
                tradingName,
                user.vrn,
                user.isAgent,
                user.arn
              ),
              Some(controllers.tradingName.routes.ConfirmTradingNameController.updateTradingName().url)
            )
            Redirect(controllers.routes.ChangeSuccessController.tradingName())
              .addingToSession(tradingNameChangeSuccessful -> "true", inFlightTradingNameChangeKey -> "tradingName")
              .removingFromSession(validationTradingNameKey)

          case Left(ErrorModel(CONFLICT, _)) =>
            logWarn("[ConfirmTradingNameController][updateTradingName] - There is a contact details update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightTradingNameChangeKey -> "tradingName")

          case Left(_) =>
            errorHandler.showInternalServerError
        }

      case _ =>
        Future.successful(Redirect(routes.CaptureTradingNameController.show()))
    }
  }
}
