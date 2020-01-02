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

package controllers

import common.SessionKeys._
import config.AppConfig
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import models.viewModels.ChangeSuccessViewModel
import play.api.mvc._
import services.VatSubscriptionService
import views.html.templates.ChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeSuccessController @Inject()(vatSubscriptionService: VatSubscriptionService,
                                        changeSuccessView: ChangeSuccessView)
                                       (implicit val appConfig: AppConfig,
                                        mcc: MessagesControllerComponents,
                                        authComps: AuthPredicateComponents,
                                        inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def tradingName: Action[AnyContent] = authPredicate.async { implicit user =>
    sessionGuard(tradingNameChangeSuccessful, prepopulationTradingNameKey)
  }

  private[controllers] def sessionGuard(changeKey: String, prePopKey: String)(implicit user: User[_]): Future[Result] =
    user.session.get(prePopKey) match {
      case Some(value) if user.session.get(changeKey).exists(_.equals("true")) =>
        renderView(changeKey, isRemoval = value == "")
      case _ =>
        val redirectLocation: Call = changeKey match {
          case `tradingNameChangeSuccessful` => controllers.tradingName.routes.CaptureTradingNameController.show()
        }
        Future.successful(Redirect(redirectLocation))
    }

  private[controllers] def renderView(changeKey: String, isRemoval: Boolean)(implicit user: User[_]): Future[Result] =
    for {
      entityName <- if (user.isAgent) {getClientEntityName}
      else {Future.successful(None)}
    } yield {
      val viewModel =
        constructViewModel(entityName, user.session.get(verifiedAgentEmail), changeKey, isRemoval)
      Ok(changeSuccessView(viewModel))
    }

  private[controllers] def constructViewModel(entityName: Option[String],
                                              agentEmail: Option[String],
                                              changeKey: String,
                                              isRemoval: Boolean): ChangeSuccessViewModel = {
    val titleMessageKey: String = getTitleMessageKey(changeKey, isRemoval)
    ChangeSuccessViewModel(titleMessageKey, agentEmail, entityName)
  }

  private[controllers] def getTitleMessageKey(changeKey: String, isRemoval: Boolean): String =
    changeKey match {
      case `tradingNameChangeSuccessful` => "tradingNameChangeSuccess.title" + (if(isRemoval) ".remove" else ".change")
    }

  private[controllers] def getClientEntityName(implicit user: User[_]): Future[Option[String]] =
    user.session.get(mtdVatAgentClientName) match {
      case Some(entityName) => Future.successful(Some(entityName))
      case None => vatSubscriptionService.getCustomerInfo(user.vrn).map { result =>
        result.fold(_ => None, details => details.entityName)
      }
    }
}
