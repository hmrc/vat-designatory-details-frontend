/*
 * Copyright 2021 HM Revenue & Customs
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
import audit.models.{ChangedBusinessNameAuditModel, ChangedTradingNameAuditModel}
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.customerInformation.{UpdateBusinessName, UpdateTradingName}
import models.errors.ErrorModel
import models.viewModels.CheckYourAnswersViewModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil.logWarn
import views.html.businessTradingName.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject() (val errorHandler: ErrorHandler,
                                            checkYourAnswersView: CheckYourAnswersView,
                                            vatSubscriptionService: VatSubscriptionService)(
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

  def updateTradingName(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>

    val currentTradingName: Option[String] = user.session.get(validationTradingNameKey) match {
      case name@Some(tName) if tName.nonEmpty => name
      case _ => None
    }

    user.session.get(prepopulationTradingNameKey) match {
      case Some(prepopTradingName) =>

        val orgDetails = UpdateTradingName(
          tradingName = if(prepopTradingName.nonEmpty) Some(prepopTradingName) else None,
          capacitorEmail = user.session.get(verifiedAgentEmail)
        )

        vatSubscriptionService.updateTradingName(user.vrn, orgDetails) map {
          case Right(successModel) =>
            auditingService.audit(ChangedTradingNameAuditModel(
              currentTradingName,
              prepopTradingName,
              user.vrn,
              user.isAgent,
              user.arn,
              OK,
              successModel.formBundle),
              Some(routes.CheckYourAnswersController.updateTradingName().url)
            )
            Redirect(controllers.routes.ChangeSuccessController.tradingName())
              .addingToSession(tradingNameChangeSuccessful -> "true", inFlightOrgDetailsKey -> "true")

          case Left(ErrorModel(CONFLICT, errorMessage)) =>
            logWarn("[CheckYourAnswersController][updateTradingName] - There is an organisation details update request " +
            "already in progress. Redirecting user to manage-vat overview page.")
            auditingService.audit(ChangedTradingNameAuditModel(
              currentTradingName, prepopTradingName, user.vrn, user.isAgent, user.arn, CONFLICT, errorMessage),
              Some(routes.CheckYourAnswersController.updateTradingName().url)
            )
            Redirect(appConfig.manageVatSubscriptionServicePath).addingToSession(inFlightOrgDetailsKey -> "true")

          case Left(ErrorModel(status, errorMessage)) =>
            auditingService.audit(ChangedTradingNameAuditModel(
              currentTradingName, prepopTradingName, user.vrn, user.isAgent, user.arn, status, errorMessage),
              Some(routes.CheckYourAnswersController.updateTradingName().url)
            )
            errorHandler.showInternalServerError
        }

      case _ =>
        Future.successful(Redirect(controllers.tradingName.routes.CaptureTradingNameController.show()))
    }
  }

  def showBusinessName: Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate) { implicit user =>
    if (appConfig.features.businessNameR19_R20Enabled()) {
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
    } else {
      errorHandler.showNotFoundError
    }
  }

  def updateBusinessName(): Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate).async { implicit user =>
    if (appConfig.features.businessNameR19_R20Enabled()) {

      (user.session.get(validationBusinessNameKey), user.session.get(prepopulationBusinessNameKey)) match {
        case (Some(currentBusinessName), Some(requestedBusinessName)) =>

          val updatedBusinessName = UpdateBusinessName(requestedBusinessName, capacitorEmail = user.session.get(verifiedAgentEmail))

          vatSubscriptionService.updateBusinessName(user.vrn, updatedBusinessName) map {
            case Right(successModel) =>
              auditingService.audit(ChangedBusinessNameAuditModel(
                currentBusinessName, requestedBusinessName, user.vrn, user.isAgent, user.arn, OK, successModel.formBundle),
                Some(routes.CheckYourAnswersController.updateBusinessName().url)
              )
              Redirect(controllers.routes.ChangeSuccessController.businessName())
              .addingToSession(businessNameChangeSuccessful -> "true", inFlightOrgDetailsKey -> "true")
            case Left(ErrorModel(CONFLICT, errorMessage)) =>
              logWarn("[CheckYourAnswersController][updateBusinessName] - There is an organisation details update request " +
                "already in progress. Redirecting user to manage-vat overview page.")
              auditingService.audit(ChangedBusinessNameAuditModel(
                currentBusinessName, requestedBusinessName, user.vrn, user.isAgent, user.arn, CONFLICT, errorMessage),
                Some(routes.CheckYourAnswersController.updateBusinessName().url)
              )
              Redirect(appConfig.manageVatSubscriptionServicePath)
                .addingToSession(inFlightOrgDetailsKey -> "true")

            case Left(ErrorModel(status, errorMessage)) =>
              auditingService.audit(ChangedBusinessNameAuditModel(
                currentBusinessName, requestedBusinessName, user.vrn, user.isAgent, user.arn, status, errorMessage),
                Some(routes.CheckYourAnswersController.updateBusinessName().url)
              )
              errorHandler.showInternalServerError
          }

        case _ =>
          Future.successful(Redirect(controllers.businessName.routes.CaptureBusinessNameController.show()))
      }
    } else {
      Future.successful(errorHandler.showNotFoundError)
    }
  }

}
