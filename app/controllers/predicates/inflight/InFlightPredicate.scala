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

package controllers.predicates.inflight

import common.SessionKeys.{inFlightOrgDetailsKey, businessNameAccessPermittedKey}
import config.AppConfig
import models.User
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.{Conflict, Redirect}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class InFlightPredicate(inFlightComps: InFlightPredicateComponents,
                        redirectURL: String,
                        businessNameJourney: Boolean) extends ActionRefiner[User, User] with I18nSupport {

  implicit val appConfig: AppConfig = inFlightComps.appConfig
  implicit val executionContext: ExecutionContext = inFlightComps.ec
  implicit val messagesApi: MessagesApi = inFlightComps.messagesApi

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val req: User[A] = request

    val accessPermitted: Option[String] =
      if(businessNameJourney) req.session.get(businessNameAccessPermittedKey) else Some("true")

    (accessPermitted, req.session.get(inFlightOrgDetailsKey)) match {
      case (Some("true"), Some("false")) => Future.successful(Right(req))
      case (Some("true"), Some("true")) => Future.successful(Left(Conflict(inFlightComps.inFlightChangeView())))
      case (Some("false"), _) if businessNameJourney => Future.successful(Left(Redirect(appConfig.manageVatSubscriptionServicePath)))
      case _ => inFlightComps.getCustomerInfoCall(req.vrn, redirectURL, businessNameJourney)

    }
  }
}
