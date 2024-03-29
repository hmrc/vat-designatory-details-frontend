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

import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.UpdateOrganisationDetailsHttpParser.UpdateOrganisationDetailsResponse
import models.customerInformation.{UpdateBusinessName, UpdateTradingName}
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import services.VatSubscriptionService

import scala.concurrent.Future

trait MockVatSubscriptionService extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockVatSubscriptionService)
  }

  val mockVatSubscriptionService: VatSubscriptionService = mock[VatSubscriptionService]

  def mockUpdateTradingName(vrn: String, orgDetails: UpdateTradingName)(response: Future[UpdateOrganisationDetailsResponse]): Unit =
    when(mockVatSubscriptionService.updateTradingName(argEq(vrn), argEq(orgDetails))(any(), any())) thenReturn response

  def mockUpdateBusinessName(vrn: String, businessName: UpdateBusinessName)(response: Future[UpdateOrganisationDetailsResponse]): Unit =
    when(mockVatSubscriptionService.updateBusinessName(argEq(vrn), argEq(businessName))(any(), any())) thenReturn response

  def mockGetCustomerInfo(vrn: String)(response: GetCustomerInfoResponse): Unit =
    when(mockVatSubscriptionService.getCustomerInfo(argEq(vrn))(any(), any())) thenReturn Future.successful(response)
}
