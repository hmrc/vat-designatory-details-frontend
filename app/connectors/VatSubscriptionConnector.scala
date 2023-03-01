/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import config.AppConfig
import connectors.httpParsers.ResponseHttpParser.HttpResult
import javax.inject.{Inject, Singleton}
import models.customerInformation.{CustomerInformation, UpdateBusinessName, UpdateOrganisationDetailsSuccess, UpdateTradingName}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.LoggerUtil

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatSubscriptionConnector @Inject()(http: HttpClient,
                                         appConfig: AppConfig) extends LoggerUtil {

  private[connectors] def getCustomerInfoUrl(vrn: String): String =
    s"${appConfig.vatSubscriptionHost}/vat-subscription/$vrn/full-information"

  private[connectors] def updateTradingNameUrl(vrn: String): String =
    s"${appConfig.vatSubscriptionHost}/vat-subscription/$vrn/trading-name"

  private[connectors] def updateBusinessNameUrl(vrn: String): String =
    s"${appConfig.vatSubscriptionHost}/vat-subscription/$vrn/business-name"

  def getCustomerInfo(vrn: String)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[CustomerInformation]] = {

    import connectors.httpParsers.GetCustomerInfoHttpParser.CustomerInfoReads

    http.GET[HttpResult[CustomerInformation]](getCustomerInfoUrl(vrn)).map {
      case customerInfo@Right(_) =>
        logger.debug(s"[VatSubscriptionConnector][getCustomerInfo] successfully received customer info response")
        customerInfo
      case httpError@Left(error) =>
        logger.warn("[VatSubscriptionConnector][getCustomerInfo] received error - " + error.message)
        httpError
    }
  }

  def updateTradingName(vrn: String, orgDetails: UpdateTradingName)
                       (implicit hc: HeaderCarrier,
                                ec: ExecutionContext): Future[HttpResult[UpdateOrganisationDetailsSuccess]] = {

    import connectors.httpParsers.UpdateOrganisationDetailsHttpParser.UpdateOrganisationDetailsReads

    http.PUT[UpdateTradingName, HttpResult[UpdateOrganisationDetailsSuccess]](updateTradingNameUrl(vrn), orgDetails)

  }


  def updateBusinessName(vrn: String, businessName: UpdateBusinessName)
                        (implicit hc: HeaderCarrier,
                         ec: ExecutionContext): Future[HttpResult[UpdateOrganisationDetailsSuccess]] = {

    import connectors.httpParsers.UpdateOrganisationDetailsHttpParser.UpdateOrganisationDetailsReads

    http.PUT[UpdateBusinessName, HttpResult[UpdateOrganisationDetailsSuccess]](updateBusinessNameUrl(vrn), businessName)
  }

}
