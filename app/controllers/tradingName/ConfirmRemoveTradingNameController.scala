/*
 * Copyright 2019 HM Revenue & Customs
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
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import views.html.tradingName.ConfirmRemoveTradingNameView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmRemoveTradingNameController @Inject()(val errorHandler: ErrorHandler,
                                               val vatSubscriptionService: VatSubscriptionService,
                                               confirmRemoveTradingName: ConfirmRemoveTradingNameView)
                                              (implicit val appConfig: AppConfig,
                                               mcc: MessagesControllerComponents,
                                               authComps: AuthPredicateComponents,
                                               inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>

    extractSessionTradingNameAddress(user) match {
      case Some(tradingName) =>
        Future.successful(Ok(confirmRemoveTradingName(tradingName)))
      case _ =>
        Future.successful(Redirect(routes.CaptureTradingNameController.show()))
      }
  }

  def removeTradingNameAddress(): Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate).async { implicit user =>

    extractSessionTradingNameAddress(user) match {
      case Some(_) =>
        Future.successful(Redirect(routes.ConfirmTradingNameController.updateTradingName())
          .addingToSession(prepopulationTradingNameKey -> ""))
      case _ =>
        Future.successful(Redirect(routes.CaptureTradingNameController.show()))
    }
  }

  private[controllers] def extractSessionTradingNameAddress(user: User[AnyContent]): Option[String] =
    user.session.get(validationTradingNameKey).filter(_.nonEmpty)
}
