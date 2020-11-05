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

package controllers.tradingName

import assets.CustomerInfoConstants.fullCustomerInfoModel
import audit.AuditingService
import common.SessionKeys
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import controllers.ControllerBaseSpec
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.errors.NotFoundView
import views.html.tradingName.CaptureTradingNameView

import scala.concurrent.{ExecutionContext, Future}

class CaptureTradingNameControllerSpec extends ControllerBaseSpec {
  val testValidationTradingName: String = "Current Trading Name"
  val testValidTradingName: String      = "A Valid Trading Name"
  val testInvalidTradingName: String    = ""

  def setup(result: GetCustomerInfoResponse): Any =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  def target(result: GetCustomerInfoResponse = Right(fullCustomerInfoModel)): CaptureTradingNameController = {
    setup(result)

    new CaptureTradingNameController(
      mockVatSubscriptionService,
      mockErrorHandler,
      inject[AuditingService],
      inject[CaptureTradingNameView])
  }

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user's current trading name is retrieved from session" should {

        lazy val result = target().show(request.withSession(
          common.SessionKeys.validationTradingNameKey -> testValidationTradingName)
        )

        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the user's current trading name" in {
          document.select("#trading-name").attr("value") shouldBe testValidationTradingName
        }

        "not call the VatSubscription service" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }

      }
    }

    "the previous form value is retrieved from session" should {

      lazy val result = target().show(request.withSession(
        common.SessionKeys.validationTradingNameKey -> testValidationTradingName,
        common.SessionKeys.prepopulationTradingNameKey -> testValidTradingName)
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
        document.select("#trading-name").attr("value") shouldBe testValidTradingName
      }

      "not call the VatSubscription service" in {
        verify(mockVatSubscriptionService, never())
          .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "there is no trading name in session" when {

      "the customerInfo call succeeds" should {

        lazy val result = target().show(request)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the customerInfo result" in {
          document.select("#trading-name").attr("value") shouldBe "PepsiMac"
        }
      }

      "the customerInfo call fails" should {

        lazy val result = target(Left(ErrorModel(
          Status.INTERNAL_SERVER_ERROR,
          "error"
        ))).show(request)

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = target().show(request)

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

      lazy val result = target().show(request)

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

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "there is a trading name in session" when {

        "the form is successfully submitted" should {

          lazy val result = target().submit(request
            .withFormUrlEncodedBody("trading-name" -> testValidTradingName)
            .withSession(common.SessionKeys.validationTradingNameKey -> testValidationTradingName))

          "redirect to the confirm trading name view" in {
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CheckYourAnswersController.show().url)
          }

          "add the new trading name to the session" in {
            session(result).get(SessionKeys.prepopulationTradingNameKey) shouldBe Some(testValidTradingName)
          }
        }

        "the form is unsuccessfully submitted" should {

          lazy val result = target().submit(request
            .withFormUrlEncodedBody("trading-name" -> testInvalidTradingName)
            .withSession(common.SessionKeys.validationTradingNameKey -> testValidationTradingName))

          "reload the page with errors" in {
            status(result) shouldBe Status.BAD_REQUEST
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }
        }
      }

      "there is no trading name in session" when {

        lazy val result = target().submit(request)

        "render the error view" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = target().submit(request)

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

      lazy val result = target().submit(request)

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
