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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.ContactPreference
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.UpdateOrganisationDetailsHttpParser.UpdateOrganisationDetailsResponse
import helpers.IntegrationBaseSpec
import models.{ChangeIndicators, User}
import models.customerInformation._
import models.errors.ErrorModel
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import stubs.VatSubscriptionStub
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext

class VatSubscriptionConnectorISpec extends IntegrationBaseSpec {

  private trait Test {
    def setupStubs(): StubMapping
    val connector: VatSubscriptionConnector = inject[VatSubscriptionConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: ExecutionContext = ExecutionContext.global
  }

  val testVrn: String = "123456789"
  val testEmail: String = "test@exmaple.com"
  implicit val testUser: models.User[AnyContentAsEmpty.type] = User(testVrn)(FakeRequest())
  val changeIndicators: ChangeIndicators = ChangeIndicators(true)

  val testCustomerInfo: CustomerInformation = CustomerInformation(
    pendingChanges = None,
    firstName = Some("Dave"),
    lastName = Some("Taylor"),
    organisationName = Some("D Taylor's Cars"),
    tradingName = Some("DT Autos"),
    contactPreference = Some(ContactPreference.digital),
    changeIndicators = Some(changeIndicators),
    nameIsReadOnly = Some(false),
    partyType = Some("1"),
    isInsolvent = false,
    continueToTrade = Some(true),
    insolvencyType = None
  )

  "Calling getCustomerInfo" when {

    "valid JSON is returned by the endpoint" should {

      "return a CustomerInformation model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubCustomerInfo

        setupStubs()

        val expected = Right(testCustomerInfo)
        val result: GetCustomerInfoResponse = await(connector.getCustomerInfo(testVrn))

        result shouldBe expected
      }
    }

    "the endpoint returns an unexpected status" should {

      "return an error model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubCustomerInfoError

        setupStubs()

        val expected = Left(ErrorModel(INTERNAL_SERVER_ERROR, """{"fail":"nope"}"""))
        val result: GetCustomerInfoResponse = await(connector.getCustomerInfo(testVrn))

        result shouldBe expected
      }
    }
  }

  "Calling updateBusinessName" when {

    "valid JSON is returned by the endpoint" should {

      "return an UpdateOrganisationDetailsSuccess model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubUpdateBusinessName

        setupStubs()

        val expected = Right(UpdateOrganisationDetailsSuccess("success"))
        val result: UpdateOrganisationDetailsResponse = await(connector.updateBusinessName(testVrn, UpdateBusinessName("Business name", None)))

        result shouldBe expected
      }
    }

    "the endpoint returns an unexpected status" should {

      "return an error model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubUpdateBusinessNameError

        setupStubs()

        val expected = Left(ErrorModel(INTERNAL_SERVER_ERROR, """{"bad":"things"}"""))
        val result: UpdateOrganisationDetailsResponse = await(connector.updateBusinessName(testVrn, UpdateBusinessName("Business name", None)))


        result shouldBe expected
      }
    }
  }

  "Calling updateTradingName" when {

    "valid JSON is returned by the endpoint" should {

      "return an UpdateOrganisationDetailsSuccess model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubUpdateTradingName

        setupStubs()

        val expected = Right(UpdateOrganisationDetailsSuccess("success"))
        val result: UpdateOrganisationDetailsResponse = await(connector.updateTradingName(testVrn, UpdateTradingName(Some("Trey Derr"), None)))

        result shouldBe expected
      }
    }

    "the endpoint returns an unexpected status" should {

      "return an error model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubUpdateTradingNameError

        setupStubs()

        val expected = Left(ErrorModel(INTERNAL_SERVER_ERROR, """{"bad":"things"}"""))
        val result: UpdateOrganisationDetailsResponse = await(connector.updateTradingName(testVrn, UpdateTradingName(Some("Trey Derr"), None)))


        result shouldBe expected
      }
    }
  }
}
