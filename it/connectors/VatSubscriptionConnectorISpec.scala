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

package connectors

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.UpdateOrganisationDetailsHttpParser.UpdateOrganisationDetailsResponse
import helpers.IntegrationBaseSpec
import models.User
import models.customerInformation._
import models.errors.ErrorModel
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import stubs.VatSubscriptionStub
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class VatSubscriptionConnectorISpec extends IntegrationBaseSpec {

  private trait Test {
    def setupStubs(): StubMapping
    val connector: VatSubscriptionConnector = inject[VatSubscriptionConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: ExecutionContext = ExecutionContext.global
  }

  val testVrn: String = "23456789"
  val testEmail: String = "test@exmaple.com"
  implicit val testUser: models.User[AnyContentAsEmpty.type] = User("999999999")(FakeRequest())

  val testCustomerInfo: CustomerInformation = CustomerInformation(
    pendingChanges = None,
    firstName = Some("Dave"),
    lastName = Some("Taylor"),
    organisationName = Some("D Taylor's Cars"),
    tradingName = Some("DT Autos")
  )

  "Calling getCustomerInfo" when {

    "valid JSON is returned by the endpoint" should {

      "return a CustomerInformation model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubCustomerInfo

        setupStubs()

        val expected = Right(testCustomerInfo)
        val result: GetCustomerInfoResponse = await(connector.getCustomerInfo("123456789"))

        result shouldBe expected
      }
    }

    "the endpoint returns an unexpected status" should {

      "return an error model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubCustomerInfoError

        setupStubs()

        val expected = Left(ErrorModel(INTERNAL_SERVER_ERROR, """{"fail":"nope"}"""))
        val result: GetCustomerInfoResponse = await(connector.getCustomerInfo("123456789"))

        result shouldBe expected
      }
    }
  }
}