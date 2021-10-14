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

package services

import assets.CustomerInfoConstants._
import mocks.MockVatSubscriptionConnector
import utils.TestUtil
import models.User
import models.customerInformation.{UpdateBusinessName, UpdateOrganisationDetailsSuccess}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class VatSubscriptionServiceSpec extends TestUtil with MockVatSubscriptionConnector {

  val service = new VatSubscriptionService(connector)
  val testVrn: String = "123456789"
  implicit val testUser: User[AnyContentAsEmpty.type] = user

  "calling getCustomerInfo" when {

    "the VatSubscriptionConnector returns a model" should {

      "return the model" in {
        mockGetCustomerInfoSuccessResponse()

        val result = await(service.getCustomerInfo(testVrn))
        result shouldBe Right(fullCustomerInfoModel)
      }
    }

    "the VatSubscriptionConnector returns an error" should {

      "return the error" in {
        mockGetCustomerInfoFailureResponse()

        val result = await(service.getCustomerInfo(testVrn))
        result shouldBe Left(invalidJsonError)
      }
    }
  }

  "calling updateBusinessName" when {

    "the VatSubscriptionConnector returns a model" should {

      "return the model" in {
        mockUpdateBusinessNameResponse(Future.successful(Right(UpdateOrganisationDetailsSuccess("success"))))

        val result = await(service.updateBusinessName(testVrn, UpdateBusinessName("The Business", None)))
        result shouldBe Right(UpdateOrganisationDetailsSuccess("success"))
      }
    }

    "the VatSubscriptionConnector returns an error" should {

      "return the error" in {
        mockUpdateBusinessNameResponse(Future.successful(Left(invalidJsonError)))

        val result = await(service.updateBusinessName(testVrn, UpdateBusinessName("The Business", None)))
        result shouldBe Left(invalidJsonError)
      }
    }
  }
}
