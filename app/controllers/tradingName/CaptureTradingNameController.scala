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
import common.SessionKeys.{prepopulationTradingNameKey, validationTradingNameKey}
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.TradingNameForm.tradingNameForm
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import views.html.tradingName.CaptureTradingNameView

import scala.concurrent.Future

@Singleton
class CaptureTradingNameController @Inject()(val errorHandler: ErrorHandler,
                                             val auditService: AuditingService,
                                             captureTradingNameView: CaptureTradingNameView)
                                            (implicit val appConfig: AppConfig,
                                             mcc: MessagesControllerComponents,
                                             authComps: AuthPredicateComponents,
                                             inFlightComps: InFlightPredicateComponents) extends BaseController {

  def show: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>
    user.session.get(validationTradingNameKey) match {
      case Some(validationTradingName) =>
        val prepopulationTradingName = user.session.get(prepopulationTradingNameKey).getOrElse(validationTradingName)
        Ok(captureTradingNameView(
          tradingNameForm(validationTradingName).fill(prepopulationTradingName), validationTradingName)
        )
      case _ => Redirect(routes.WhatToDoController.show())
    }
  }

  def submit: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>
    val validationTradingName: Option[String] = user.session.get(validationTradingNameKey)

    validationTradingName match {
      case Some(validation) => tradingNameForm(validation).bindFromRequest.fold(
        errorForm => {
          Future.successful(BadRequest(captureTradingNameView(errorForm, validation)))
        },
        tradingName => {
          Future.successful(Redirect(controllers.businessTradingName.routes.CheckYourAnswersController.show())
            .addingToSession(prepopulationTradingNameKey -> tradingName))
        }
      )
      case None => Future.successful(errorHandler.showInternalServerError)
    }
  }
}
