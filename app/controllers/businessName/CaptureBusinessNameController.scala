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

package controllers.businessName

import audit.AuditingService
import common.SessionKeys.{prepopulationBusinessNameKey, validationBusinessNameKey}
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.BusinessNameForm.businessNameForm
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import views.html.businessName.CaptureBusinessNameView

import scala.concurrent.Future

@Singleton
class CaptureBusinessNameController @Inject()(val errorHandler: ErrorHandler,
                                              captureBusinessNameView: CaptureBusinessNameView)
                                             (implicit val appConfig: AppConfig,
                                              mcc: MessagesControllerComponents,
                                              authComps: AuthPredicateComponents,
                                              inFlightComps: InFlightPredicateComponents) extends BaseController {

  def show: Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate) { implicit user =>
    val validationBusinessName = user.session.get(validationBusinessNameKey).getOrElse("")
    val prepopulationBusinessName = user.session.get(prepopulationBusinessNameKey).getOrElse(validationBusinessName)
    Ok(captureBusinessNameView(
      businessNameForm(validationBusinessName).fill(prepopulationBusinessName), validationBusinessName)
    )
  }

  def submit: Action[AnyContent] = (authPredicate andThen businessNameAccessPredicate).async { implicit user =>
    val validationBusinessName: Option[String] = user.session.get(validationBusinessNameKey)

    validationBusinessName match {
      case Some(validation) => businessNameForm(validation).bindFromRequest.fold(
        errorForm => {
          Future.successful(BadRequest(captureBusinessNameView(errorForm, validation)))
        },
        businessName => {
          Future.successful(Redirect(controllers.businessTradingName.routes.CheckYourAnswersController.show())
            .addingToSession(prepopulationBusinessNameKey -> businessName))
        }
      )
      case None => Future.successful(errorHandler.showInternalServerError)
    }
  }
}
