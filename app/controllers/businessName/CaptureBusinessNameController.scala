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

package controllers.businessName

import common.SessionKeys.{prepopulationBusinessNameKey, validationBusinessNameKey}
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.BusinessNameForm.businessNameForm
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.VatSubscriptionService
import views.html.businessName.CaptureBusinessNameView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureBusinessNameController @Inject()(val errorHandler: ErrorHandler,
                                              captureBusinessNameView: CaptureBusinessNameView,
                                              vatSubscriptionService: VatSubscriptionService)
                                             (implicit val appConfig: AppConfig,
                                              mcc: MessagesControllerComponents,
                                              authComps: AuthPredicateComponents,
                                              inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = authComps.executionContext

  def show(): Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate).async { implicit user =>
    if (appConfig.features.businessNameR19_R20Enabled()) {
      val validationBusinessName: Future[Option[String]] = user.session.get(validationBusinessNameKey) match {
        case Some(businessName) => Future.successful(Some(businessName))
        case _ =>
          vatSubscriptionService.getCustomerInfo(user.vrn) map {
            case Right(details) => Some(details.organisationName.getOrElse(""))
            case _ => None
          }
      }
      validationBusinessName.map {
        case Some(businessName) if businessName.nonEmpty =>
          val prepopulationBusinessName = user.session.get(prepopulationBusinessNameKey).getOrElse(businessName)
          Ok(captureBusinessNameView(
            businessNameForm(businessName).fill(prepopulationBusinessName))
          ).addingToSession(validationBusinessNameKey -> businessName)
        case _ => errorHandler.showInternalServerError
      }
    } else {
      Future.successful(errorHandler.showNotFoundError)
    }
  }

  def submit(): Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate).async { implicit user =>
    if (appConfig.features.businessNameR19_R20Enabled()) {
      val validationBusinessName: Option[String] = user.session.get(validationBusinessNameKey)

      validationBusinessName match {
        case Some(validation) => businessNameForm(validation).bindFromRequest.fold(
          errorForm => {
            Future.successful(BadRequest(captureBusinessNameView(errorForm)))
          },
          businessName => {
            Future.successful(Redirect(controllers.businessTradingName.routes.CheckYourAnswersController.showBusinessName)
              .addingToSession(prepopulationBusinessNameKey -> businessName))
          }
        )
        case None => Future.successful(errorHandler.showInternalServerError)
      }
    } else {
      Future.successful(errorHandler.showNotFoundError)
    }
  }
}
