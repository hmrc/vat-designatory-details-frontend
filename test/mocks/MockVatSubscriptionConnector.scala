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

package mocks

import assets.CustomerInfoConstants._
import connectors.VatSubscriptionConnector
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.UpdateOrganisationDetailsHttpParser.UpdateOrganisationDetailsResponse
import models.customerInformation.{UpdateBusinessName, UpdateTradingName, UpdateOrganisationDetailsSuccess}
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockVatSubscriptionConnector extends MockFactory {

  val connector: VatSubscriptionConnector = mock[VatSubscriptionConnector]

  def mockGetCustomerInfoResponse(result: Future[GetCustomerInfoResponse]): Unit = {
    (connector.getCustomerInfo(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(result)
  }

  def mockUpdateTradingNameResponse(result: Future[UpdateOrganisationDetailsResponse]): Unit = {
    (connector.updateTradingName(_: String, _: UpdateTradingName)(_: HeaderCarrier,
      _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(result)
  }

  def mockUpdateBusinessNameResponse(result: Future[UpdateOrganisationDetailsResponse]): Unit = {
    (connector.updateBusinessName(_: String, _: UpdateBusinessName)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(result)
  }

  def mockGetCustomerInfoSuccessResponse(): Unit = mockGetCustomerInfoResponse(
    Future.successful(Right(fullCustomerInfoModel)))

  def mockGetCustomerInfoFailureResponse(): Unit = mockGetCustomerInfoResponse(
    Future.successful(Left(invalidJsonError)))

  def mockUpdateTradingNameSuccessResponse(): Unit = mockUpdateTradingNameResponse(
    Future.successful(Right(UpdateOrganisationDetailsSuccess("success"))))

  def mockUpdateTradingNameFailureResponse(): Unit = mockUpdateTradingNameResponse(
    Future.successful(Left(invalidJsonError)))
}
