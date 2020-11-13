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

import common.SessionKeys.{prepopulationTradingNameKey, validationTradingNameKey}
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import javax.inject.Inject
import models.{No, Yes, YesNo}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.tradingName.ConfirmRemoveTradingNameView

import scala.concurrent.Future

class ConfirmRemoveTradingNameController @Inject()(confirmRemoveTradingNameView: ConfirmRemoveTradingNameView)
                                                  (implicit val appConfig: AppConfig,
                                                   mcc: MessagesControllerComponents,
                                                   authComps: AuthPredicateComponents,
                                                   inFlightComps: InFlightPredicateComponents) extends BaseController {

  val yesNoForm: Form[YesNo] = YesNoForm.yesNoForm("confirmRemove.error")

  def show: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>
    user.session.get(validationTradingNameKey) match {
      case Some(tradingName) if tradingName.nonEmpty =>
        Future.successful(Ok(confirmRemoveTradingNameView(yesNoForm, tradingName)))
      case _ =>
        Future.successful(Redirect(routes.CaptureTradingNameController.show().url))
    }
  }

  def submit: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>
    user.session.get(validationTradingNameKey) match {
      case Some(tradingName) =>
        yesNoForm.bindFromRequest.fold(
          errorForm => {
            BadRequest(confirmRemoveTradingNameView(errorForm, tradingName))
          },
          {
            case Yes => Redirect(routes.CheckYourAnswersController.updateTradingName())
              .addingToSession(prepopulationTradingNameKey -> "")
            case No => Redirect(appConfig.manageVatSubscriptionServicePath)
          }
        )
      case None => authComps.errorHandler.showInternalServerError
    }
  }
}
