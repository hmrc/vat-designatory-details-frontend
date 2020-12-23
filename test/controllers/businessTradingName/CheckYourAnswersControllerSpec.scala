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

package controllers.businessTradingName

import assets.BaseTestConstants.vrn
import audit.AuditingService
import common.SessionKeys.businessNameAccessPermittedKey
import controllers.ControllerBaseSpec
import models.customerInformation.{UpdateOrganisationDetailsSuccess, UpdateTradingName}
import models.errors.ErrorModel
import play.api.test.Helpers._
import views.html.businessTradingName.CheckYourAnswersView

import scala.concurrent.Future


class CheckYourAnswersControllerSpec extends ControllerBaseSpec {

  implicit val auditingService: AuditingService = inject[AuditingService]

  val controller = new CheckYourAnswersController(
    mockErrorHandler,
    inject[CheckYourAnswersView],
    mockVatSubscriptionService
  )

  "Calling the show trading name action in CheckYourAnswersController" when {

    "there is a trading name in session" should {

      "show the Check your answer page" in {
        mockIndividualAuthorised()
        val result = controller.showTradingName(requestWithTradingName)

        status(result) shouldBe OK
      }
    }

    "there isn't a trading name in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.showTradingName(request)
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to enter a new trading name" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.showTradingName(requestWithTradingName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "Calling updateTradingName() in CheckYourAnswersController" when {

    "there is a trading name in session" when {

      "the trading name has been updated successfully" should {

        lazy val result = {
          mockUpdateTradingName(vrn, UpdateTradingName(testTradingName, None))(Future(Right(UpdateOrganisationDetailsSuccess("someFormBundle"))))
          controller.updateTradingName()(requestWithTradingName)
        }

        "return 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the trading name changed success page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.tradingName().url)
        }
      }

      "VatSubscriptionService returns a conflict" should {

        lazy val result = {
          mockUpdateTradingName(vrn, UpdateTradingName(testTradingName, None))(Future(Left(ErrorModel(CONFLICT, "bad things"))))
          controller.updateTradingName()(requestWithTradingName)
        }

        "return 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to manage-vat" in {
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "VatSubscriptionService returns an error" should {

        lazy val result = {
          mockUpdateTradingName(vrn, UpdateTradingName(testTradingName, None))(Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "bad things, again"))))
          controller.updateTradingName()(requestWithTradingName)
        }

        "return 500" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "there isn't a trading name in session" should {

      lazy val result = {
        controller.updateTradingName()(request)
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the capture trading name page" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateTradingName()(requestWithTradingName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }
  "Calling the show business name action in CheckYourAnswersController" when {

    "there is a business name in session" should {

      "show the Check your answer page" in {
        mockIndividualAuthorised()
        val result = controller.showBusinessName(requestWithBusinessName.withSession(
          businessNameAccessPermittedKey -> "true"))

        status(result) shouldBe OK
      }
    }

    "there isn't a business name in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.showBusinessName(request.withSession(
          businessNameAccessPermittedKey -> "true"))
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to enter a new business name" in {
        redirectLocation(result) shouldBe Some(controllers.businessName.routes.CaptureBusinessNameController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.showBusinessName(requestWithBusinessName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "Calling updateBusinessName() in CheckYourAnswersController" when {

    "there is a business name in session" when {

      "the business name has been updated successfully" should {

        lazy val result = {
          controller.updateBusinessName()(requestWithBusinessName.withSession(
            businessNameAccessPermittedKey -> "true"))
        }

        "return 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the business name changed success page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.businessName().url)
        }
      }
    }

    "there isn't a business name in session" should {

      lazy val result = {
        controller.updateBusinessName()(request.withSession(
          businessNameAccessPermittedKey -> "true"))
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the capture business name page" in {
        redirectLocation(result) shouldBe Some(controllers.businessName.routes.CaptureBusinessNameController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateBusinessName()(requestWithBusinessName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }
}

