/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.YesNoForm
import models.{No, User, Yes, YesNo}
import javax.inject.{Inject, Singleton}
import models.customerInformation.{UpdateBusinessName, UpdateTradingName}
import models.errors.ErrorModel
import models.viewModels.CheckYourAnswersViewModel
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.VatSubscriptionService
import utils.LoggerUtil
import views.html.businessTradingName.CheckYourAnswersView
import views.html.tradingName.ConfirmRemoveTradingNameView
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject() (val errorHandler: ErrorHandler,
                                            checkYourAnswersView: CheckYourAnswersView,
                                            confirmRemoveTradingNameView: ConfirmRemoveTradingNameView,
                                            vatSubscriptionService: VatSubscriptionService)(
                                            implicit val authComps: AuthPredicateComponents,
                                            mcc: MessagesControllerComponents,
                                            inFlightComps: InFlightPredicateComponents,
                                            appConfig: AppConfig,
                                            auditingService: AuditingService
                                           ) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext
  val yesNoForm: Form[YesNo] = YesNoForm.yesNoForm("confirmRemove.error")

  def showTradingName(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>
    user.session.get(prepopulationTradingNameKey) match {
      case Some(tradingName) =>
        val viewModel = CheckYourAnswersViewModel(
          question = "checkYourAnswers.tradingName",
          answer = tradingName,
          changeLink = controllers.tradingName.routes.CaptureTradingNameController.show.url,
          changeLinkHiddenText = "checkYourAnswers.tradingName.edit",
          continueLink = controllers.businessTradingName.routes.CheckYourAnswersController.updateTradingName)
        Ok(checkYourAnswersView(viewModel))
      case _ =>
        Redirect(controllers.tradingName.routes.CaptureTradingNameController.show)
    }
  }

  private[controllers] def performTradingNameUpdate(updateTradingNameModel: UpdateTradingName, pageUrl: String)(implicit user: User[_]): Future[Result] = {
    val existingTradingName = user.session.get(validationTradingNameKey)
    val newTradingName = updateTradingNameModel.tradingName.getOrElse("")
    vatSubscriptionService.updateTradingName(user.vrn, updateTradingNameModel).map {
      case Right(successModel) =>
        auditingService.audit(
          ChangedTradingNameAuditModel(
            existingTradingName,
            newTradingName,
            user.vrn,
            user.isAgent,
            user.arn,
            OK,
            successModel.formBundle
          ),
          Some(pageUrl)
        )
        Redirect(controllers.routes.ChangeSuccessController.tradingName)
          .addingToSession(tradingNameChangeSuccessful -> "true", inFlightOrgDetailsKey -> "true", prepopulationTradingNameKey -> newTradingName)

      case Left(ErrorModel(CONFLICT, errorMessage)) =>
        logger.warn("[CheckYourAnswersController][updateTradingName] - There is an organisation details update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        auditingService.audit(ChangedTradingNameAuditModel(
          existingTradingName, newTradingName, user.vrn, user.isAgent, user.arn, CONFLICT, errorMessage),
          Some(routes.CheckYourAnswersController.updateTradingName.url)
        )
        Redirect(appConfig.manageVatSubscriptionServicePath).addingToSession(inFlightOrgDetailsKey -> "true")

      case Left(ErrorModel(status, errorMessage)) =>
        auditingService.audit(ChangedTradingNameAuditModel(
          existingTradingName, newTradingName, user.vrn, user.isAgent, user.arn, status, errorMessage),
          Some(routes.CheckYourAnswersController.updateTradingName.url)
        )
        errorHandler.showInternalServerError
    }
  }

  def updateTradingName(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>

    user.session.get(prepopulationTradingNameKey) match {
      case Some(prepopTradingName) =>

        val orgDetails = UpdateTradingName(
          tradingName = Some(prepopTradingName),
          capacitorEmail = user.session.get(mtdVatvcVerifiedAgentEmail)
        )
        performTradingNameUpdate(orgDetails, routes.CheckYourAnswersController.updateTradingName.url)

      case _ =>
        Future.successful(Redirect(controllers.tradingName.routes.CaptureTradingNameController.show))
    }
  }

  def showConfirmTradingNameRemoval(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>
    user.session.get(validationTradingNameKey) match {
      case Some(tradingName) if tradingName.nonEmpty =>
        Future.successful(Ok(confirmRemoveTradingNameView(yesNoForm, tradingName)))
      case _ =>
        Future.successful(Redirect(controllers.tradingName.routes.CaptureTradingNameController.show.url))
    }
  }

  def removeTradingName(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>
    user.session.get(validationTradingNameKey) match {
      case Some(tradingName) =>
        yesNoForm.bindFromRequest.fold(
          errorForm => {
            Future.successful(BadRequest(confirmRemoveTradingNameView(errorForm, tradingName)))
          },
          {
            case Yes => performTradingNameUpdate(
              UpdateTradingName(
                None, user.session.get(mtdVatvcVerifiedAgentEmail)
              ),
              controllers.businessTradingName.routes.CheckYourAnswersController.removeTradingName.url
            )
            case No => Future.successful(Redirect(appConfig.manageVatSubscriptionServicePath))
          }
        )
      case None => Future.successful(authComps.errorHandler.showInternalServerError)
    }
  }

  def showBusinessName(): Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate) { implicit user =>
    user.session.get(prepopulationBusinessNameKey) match {
      case Some(businessName) =>
        val viewModel = CheckYourAnswersViewModel(
          question = "checkYourAnswers.businessName",
          answer = businessName,
          changeLink = controllers.businessName.routes.CaptureBusinessNameController.show.url,
          changeLinkHiddenText = "checkYourAnswers.businessName.edit",
          continueLink = controllers.businessTradingName.routes.CheckYourAnswersController.updateBusinessName)
        Ok(checkYourAnswersView(viewModel))
      case _ =>
        Redirect(controllers.businessName.routes.CaptureBusinessNameController.show)
    }
  }

  def updateBusinessName(): Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate).async { implicit user =>
    (user.session.get(validationBusinessNameKey), user.session.get(prepopulationBusinessNameKey)) match {
      case (Some(currentBusinessName), Some(requestedBusinessName)) =>

        val updatedBusinessName = UpdateBusinessName(requestedBusinessName, capacitorEmail = user.session.get(mtdVatvcVerifiedAgentEmail))

        vatSubscriptionService.updateBusinessName(user.vrn, updatedBusinessName) map {
          case Right(successModel) =>
            auditingService.audit(ChangedBusinessNameAuditModel(
              currentBusinessName, requestedBusinessName, user.vrn, user.isAgent, user.arn, OK, successModel.formBundle),
              Some(routes.CheckYourAnswersController.updateBusinessName.url)
            )
            Redirect(controllers.routes.ChangeSuccessController.businessName)
              .addingToSession(businessNameChangeSuccessful -> "true", inFlightOrgDetailsKey -> "true")
          case Left(ErrorModel(CONFLICT, errorMessage)) =>
            logger.warn("[CheckYourAnswersController][updateBusinessName] - There is an organisation details update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            auditingService.audit(ChangedBusinessNameAuditModel(
              currentBusinessName, requestedBusinessName, user.vrn, user.isAgent, user.arn, CONFLICT, errorMessage),
              Some(routes.CheckYourAnswersController.updateBusinessName.url)
            )
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightOrgDetailsKey -> "true")

          case Left(ErrorModel(status, errorMessage)) =>
            auditingService.audit(ChangedBusinessNameAuditModel(
              currentBusinessName, requestedBusinessName, user.vrn, user.isAgent, user.arn, status, errorMessage),
              Some(routes.CheckYourAnswersController.updateBusinessName.url)
            )
            errorHandler.showInternalServerError
        }

      case _ =>
        Future.successful(Redirect(controllers.businessName.routes.CaptureBusinessNameController.show))
    }
  }

}
