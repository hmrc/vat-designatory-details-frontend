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
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.errors.ErrorModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil.logWarn
import views.html.tradingName.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject() (checkYourAnswersView: CheckYourAnswersView,
                                            auditService: AuditingService,
                                            errorHandler: ErrorHandler)(
                                            implicit val authComps: AuthPredicateComponents,
                                            mcc: MessagesControllerComponents,
                                            inFlightComps: InFlightPredicateComponents,
                                            appConfig: AppConfig
                                           ) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>
    user.session.get(prepopulationTradingNameKey) match {
      case Some(tradingName) =>
        Ok(checkYourAnswersView(tradingName))
      case _ =>
        Redirect(routes.CaptureTradingNameController.show())
    }
  }


  def updateTradingName(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>

    user.session.get(prepopulationTradingNameKey) match {
      case Some(_) =>
            Redirect(controllers.routes.ChangeSuccessController.tradingName())
              .addingToSession(tradingNameChangeSuccessful -> "true", inFlightTradingNameChangeKey -> "true")
      case _ =>
        Redirect(routes.CaptureTradingNameController.show())
    }
  }
}
