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

package controllers.businessName

import assets.CustomerInfoConstants.{customerInfoNoPending, fullCustomerInfoModel}
import common.SessionKeys.{prepopulationBusinessNameKey, validationBusinessNameKey}
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import controllers.ControllerBaseSpec
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.businessName.CaptureBusinessNameView

import scala.concurrent.{ExecutionContext, Future}

class CaptureBusinessNameControllerSpec extends ControllerBaseSpec {

  def setup(result: GetCustomerInfoResponse): Any =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  def target(result: GetCustomerInfoResponse = Right(customerInfoNoPending)): CaptureBusinessNameController = {
    setup(result)
    new CaptureBusinessNameController(mockErrorHandler, inject[CaptureBusinessNameView], mockVatSubscriptionService)
  }

  val testValidationBusinessName: String = "Current Business Name"
  val testValidBusinessName: String      = "A Valid Business Name"
  val testInvalidBusinessName: String    = ""

  val controller = new CaptureBusinessNameController(
    mockErrorHandler,
    inject[CaptureBusinessNameView],
    mockVatSubscriptionService
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user's current business name is retrieved from session" should {

        lazy val result = target().show(request.withSession(validationBusinessNameKey -> testValidationBusinessName))

        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the user's current business name" in {
          document.select("#business-name").attr("value") shouldBe testValidationBusinessName
        }
      }

      "the previous form value is retrieved from session" should {

        lazy val result = target().show(request.withSession(
          validationBusinessNameKey -> testValidationBusinessName,
          prepopulationBusinessNameKey -> testValidBusinessName)
        )
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the previously entered form value" in {
          document.select("#business-name").attr("value") shouldBe testValidBusinessName
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = controller.show(request)

      "return 403" in {
        mockIndividualWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = controller.show(request)

      "return 401" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

}
