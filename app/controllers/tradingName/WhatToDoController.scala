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

package controllers.tradingName

import common.SessionKeys.validationTradingNameKey
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.WhatToDoForm.whatToDoForm
import javax.inject.{Inject, Singleton}
import models.{Change, Remove}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import views.html.tradingName.WhatToDoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatToDoController @Inject()(whatToDoView: WhatToDoView,
                                   vatSubscriptionService: VatSubscriptionService)
                                  (implicit val appConfig: AppConfig,
                                   mcc: MessagesControllerComponents,
                                   authComps: AuthPredicateComponents,
                                   inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>
    val validationTradingName: Future[Option[String]] = user.session.get(validationTradingNameKey) match {
      case Some(tradingName) => Future.successful(Some(tradingName))
      case _ =>
        vatSubscriptionService.getCustomerInfo(user.vrn) map {
          case Right(details) => Some(details.tradingName.getOrElse(""))
          case _ => None
        }
    }
    validationTradingName.map {
      case Some("") =>
        Redirect(routes.CaptureTradingNameController.show()).addingToSession(validationTradingNameKey -> "")
      case Some(value) => Ok(whatToDoView(whatToDoForm)).addingToSession(validationTradingNameKey -> value)
      case None => authComps.errorHandler.showInternalServerError
    }
  }

  def submit: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>
    user.session.get(validationTradingNameKey) match {
      case Some(_) =>
        whatToDoForm.bindFromRequest.fold(
          errorForm => {
            BadRequest(whatToDoView(errorForm))
          },
          {
            case Change => Redirect(routes.CaptureTradingNameController.show())
            case Remove => Redirect(routes.ConfirmRemoveTradingNameController.show())
          }
        )
      case None => authComps.errorHandler.showInternalServerError
    }
  }
}
