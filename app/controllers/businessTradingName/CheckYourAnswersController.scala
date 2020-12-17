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

package controllers.businessTradingName

import audit.AuditingService
import audit.models.ChangedTradingNameAuditModel
import common.SessionKeys._
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import models.viewModels.CheckYourAnswersViewModel
import views.html.businessTradingName.CheckYourAnswersView

import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject() (checkYourAnswersView: CheckYourAnswersView)(
                                            implicit val authComps: AuthPredicateComponents,
                                            mcc: MessagesControllerComponents,
                                            inFlightComps: InFlightPredicateComponents,
                                            appConfig: AppConfig,
                                            auditingService: AuditingService
                                           ) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def showTradingName: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>
    user.session.get(prepopulationTradingNameKey) match {
      case Some(tradingName) =>
        val viewModel = CheckYourAnswersViewModel(
          question = "checkYourAnswers.tradingName",
          answer = tradingName,
          changeLink = controllers.tradingName.routes.CaptureTradingNameController.show().url,
          continueLink = controllers.businessTradingName.routes.CheckYourAnswersController.updateTradingName().url)
        Ok(checkYourAnswersView(viewModel))
      case _ =>
        Redirect(controllers.tradingName.routes.CaptureTradingNameController.show())
    }
  }

  def updateTradingName(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>

    val currentTradingName: Option[String] = user.session.get(validationTradingNameKey) match {
      case Some("") => None
      case Some(name) => Some(name)
      case _ => None
    }

    user.session.get(prepopulationTradingNameKey) match {
      case Some(prepopTradingName) =>
        auditingService.audit(ChangedTradingNameAuditModel(
          currentTradingName,
          prepopTradingName,
          user.vrn,
          user.isAgent,
          user.arn),
          Some(routes.CheckYourAnswersController.updateTradingName().url)
        )
        Redirect(controllers.routes.ChangeSuccessController.tradingName())
          .addingToSession(tradingNameChangeSuccessful -> "true", inFlightOrgDetailsKey -> "true")

      case _ =>
        Redirect(controllers.tradingName.routes.CaptureTradingNameController.show())
    }
  }


  def showBusinessName: Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate) { implicit user =>
    user.session.get(prepopulationBusinessNameKey) match {
      case Some(businessName) =>
        val viewModel = CheckYourAnswersViewModel(
          question = "checkYourAnswers.businessName",
          answer = businessName,
          changeLink = controllers.businessName.routes.CaptureBusinessNameController.show().url,
          continueLink = controllers.businessTradingName.routes.CheckYourAnswersController.updateBusinessName().url)
        Ok(checkYourAnswersView(viewModel))
      case _ =>
        Redirect(controllers.businessName.routes.CaptureBusinessNameController.show())
    }
  }
  def updateBusinessName(): Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate) { implicit user =>

    user.session.get(prepopulationBusinessNameKey) match {
      case Some(businessName) =>
        Redirect(controllers.routes.ChangeSuccessController.businessName())
          .addingToSession(businessNameChangeSuccessful -> "true", inFlightOrgDetailsKey -> "true")
      case _ =>
        Redirect(controllers.businessName.routes.CaptureBusinessNameController.show())
    }
  }

}
