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

package controllers.businessName

import assets.CustomerInfoConstants.{customerInfoNoBusinessName, customerInfoNoPending, fullCustomerInfoModel, invalidJsonError}
import common.SessionKeys
import common.SessionKeys.{businessNameAccessPermittedKey, prepopulationBusinessNameKey, validationBusinessNameKey}
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

    insolvencyCheck(controller.show)

    "a user is enrolled with a valid enrolment" when {

      "the user's current business name is retrieved from session" should {

        lazy val result = {
          target().show()(getRequest.withSession(
            validationBusinessNameKey -> testValidationBusinessName,
            businessNameAccessPermittedKey -> "true"))
        }

        lazy val document = Jsoup.parse(contentAsString(result))

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

        lazy val result = {
          target().show()(getRequest.withSession(
            validationBusinessNameKey -> testValidationBusinessName,
            prepopulationBusinessNameKey -> testValidBusinessName,
            businessNameAccessPermittedKey -> "true")
          )
        }
        lazy val document = Jsoup.parse(contentAsString(result))

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

      "there is no business name in session but vatSubscriptionService returns a business name" should {

        lazy val result = {
          target(Right(fullCustomerInfoModel)).show()(getRequest
            .withSession(businessNameAccessPermittedKey -> "true"))
        }

        lazy val document = Jsoup.parse(contentAsString(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the user's current business name" in {
          Some(document.select("#business-name").attr("value")) shouldBe fullCustomerInfoModel.organisationName
        }
      }

      "there is no business name in session and the user doesn't have a business name" when {

        lazy val result = {
          target(Right(customerInfoNoBusinessName)).show()(getRequestWithoutExistingBusinessName
            .withSession(businessNameAccessPermittedKey -> "true"))
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "render the error view" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }

      "there is no business name in session and the user's organisation details can't be retrieved" when {

        lazy val result = {
          target(Left(invalidJsonError)).show()(getRequest
            .withSession(businessNameAccessPermittedKey -> "true"))
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "render the error view" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user does not have a valid enrolment" should {

      lazy val result = controller.show()(getRequest)

      "return 403" in {
        mockIndividualWithoutEnrolment
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = controller.show()(getRequest)

      "return 401" in {
        mockMissingBearerToken
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "Calling the submit action" when {

      insolvencyCheck(controller.submit)

      "a user is enrolled with a valid enrolment" when {

        "there is a business name in session" when {

          "the form is successfully submitted" should {

            lazy val result = {
              controller.submit()(postRequest
                .withFormUrlEncodedBody("business-name" -> testValidBusinessName)
                .withSession(validationBusinessNameKey -> testValidationBusinessName, businessNameAccessPermittedKey -> "true"))
            }

            "redirect to the confirm business name view" in {
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.businessTradingName.routes.CheckYourAnswersController.showBusinessName.url)
            }

            "add the new business name to the session" in {
              session(result).get(SessionKeys.prepopulationBusinessNameKey) shouldBe Some(testValidBusinessName)
            }
          }

          "the form is unsuccessfully submitted" should {

            lazy val result = controller.submit()(postRequest
              .withFormUrlEncodedBody("business-name" -> testInvalidBusinessName)
              .withSession(validationBusinessNameKey -> testValidationBusinessName, businessNameAccessPermittedKey -> "true"))

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }

            "reload the page with errors" in {
              status(result) shouldBe Status.BAD_REQUEST
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }
          }
        }

        "there is no business name in session" when {

          lazy val result = controller.submit()(postRequest
            .withFormUrlEncodedBody("business-name" -> testValidBusinessName)
            .withSession(businessNameAccessPermittedKey -> "true"))

          "return 500" in {
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          }

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

        lazy val result = controller.submit()(postRequest)

        "return 403" in {
          mockIndividualWithoutEnrolment
          status(result) shouldBe Status.FORBIDDEN
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }

      "a user is not logged in" should {

        lazy val result = controller.submit()(postRequest)

        "return 401" in {
          mockMissingBearerToken
          status(result) shouldBe Status.UNAUTHORIZED
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

}
