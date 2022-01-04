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

package controllers

import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents

import javax.inject.{Inject, Singleton}
import models.User
import play.api.mvc._
import utils.LoggerUtil
import views.html.businessName.BusinessNameChangeSuccessView
import views.html.tradingName.TradingNameChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeSuccessController @Inject()(tradingNameSuccessView: TradingNameChangeSuccessView,
                                        businessNameSuccessView: BusinessNameChangeSuccessView,
                                        errorHandler: ErrorHandler)
                                       (implicit val appConfig: AppConfig,
                                        mcc: MessagesControllerComponents,
                                        authComps: AuthPredicateComponents,
                                        inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext

  def tradingName(): Action[AnyContent] = authPredicate.async { implicit user =>
    (user.session.get(tradingNameChangeSuccessful), user.session.get(validationTradingNameKey), user.session.get(prepopulationTradingNameKey)) match {
      case (Some("true"), Some(validationValue), Some(prePopValue)) =>
        renderTradingNameView(isRemoval = prePopValue == "", isAddition = validationValue == "")
      case _ =>
        Future.successful(Redirect(controllers.tradingName.routes.CaptureTradingNameController.show))
    }
  }

  def businessName(): Action[AnyContent] = authPredicate.async { implicit user =>
    if(appConfig.features.businessNameR19_R20Enabled()) {
      user.session.get(businessNameChangeSuccessful) match {
        case Some("true") =>
          Future.successful(Ok(businessNameSuccessView()))
        case _ =>
          Future.successful(Redirect(controllers.businessName.routes.CaptureBusinessNameController.show))
      }
    } else {
      Future.successful(errorHandler.showNotFoundError)
    }
  }

  private[controllers] def renderTradingNameView(isRemoval: Boolean,
                                                 isAddition: Boolean)(implicit user: User[_]): Future[Result] = {
    val titleMessageKey: Option[String] = (isAddition, isRemoval) match {
       case (true, false) => Some("tradingNameChangeSuccess.title.add")
       case (false, true) => Some("tradingNameChangeSuccess.title.remove")
       case (false, false) => Some("tradingNameChangeSuccess.title.change")
       case _ => None
    }
    titleMessageKey.fold {
      logger.warn("[ChangeSuccessController][renderView] - validation and prePop session values were both blank. " +
        "Rendering InternalServerError")
      Future.successful(authComps.errorHandler.showInternalServerError)
    } { title =>
      Future.successful(Ok(tradingNameSuccessView(title, isRemoval)))
    }
  }
}
