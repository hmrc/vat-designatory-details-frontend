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

package controllers.predicates.inflight

import common.SessionKeys.{businessNameAccessPermittedKey, inFlightOrgDetailsKey}
import config.{AppConfig, ErrorHandler}

import javax.inject.{Inject, Singleton}
import models.User
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.mvc.Results.{Conflict, Redirect}
import services.VatSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggerUtil
import views.html.errors.InFlightChangeView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InFlightPredicateComponents @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                            val errorHandler: ErrorHandler,
                                            val mcc: MessagesControllerComponents,
                                            val inFlightChangeView: InFlightChangeView)
                                           (implicit val appConfig: AppConfig,
                                            val ec: ExecutionContext,
                                            val messagesApi: MessagesApi) extends I18nSupport with LoggerUtil {

  def getCustomerInfoCall[A](vrn: String, redirectURL: String, businessNameJourney: Boolean)
                            (implicit hc: HeaderCarrier, request: User[A]): Future[Either[Result, User[A]]] =
    vatSubscriptionService.getCustomerInfo(vrn).map {
      case Right(customerInfo) =>
        val accessToBusinessNameJourney: Boolean = customerInfo.organisationName.isDefined &&
                                                   customerInfo.nameIsReadOnly.contains(false) &&
                                                   customerInfo.isValidPartyType
        val redirectLocation: String =
          if(businessNameJourney && !accessToBusinessNameJourney) appConfig.manageVatSubscriptionServicePath else redirectURL

        customerInfo.changeIndicators.map(_.organisationDetails) match {
          case Some(true) =>
            Left(Conflict(inFlightChangeView()).addingToSession(
              inFlightOrgDetailsKey -> "true",
              businessNameAccessPermittedKey -> accessToBusinessNameJourney.toString
            ))
          case _ =>
            logger.debug("[InFlightPredicateComponents][getCustomerInfoCall] - Redirecting user to the start of the journey.")
            Left(Redirect(redirectLocation).addingToSession(
              inFlightOrgDetailsKey -> "false",
              businessNameAccessPermittedKey -> accessToBusinessNameJourney.toString
            ))
        }
      case Left(error) =>
        logger.warn("[InFlightPredicateComponents][getCustomerInfoCall] - " +
          s"The call to the GetCustomerInfo API failed. Error: ${error.message}")
        Left(errorHandler.showInternalServerError)
    }
}
