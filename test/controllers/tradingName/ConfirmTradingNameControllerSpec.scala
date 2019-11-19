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

package controllers.tradingName

import assets.BaseTestConstants._
import audit.AuditingService
import audit.models.ChangedTradingNameAuditModel
import controllers.ControllerBaseSpec
import models.customerInformation.UpdateOrganisationDetailsSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.test.Helpers._
import views.html.tradingName.ConfirmTradingNameView

import scala.concurrent.Future

class ConfirmTradingNameControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmTradingNameController(
    mockErrorHandler,
    mockVatSubscriptionService,
    inject[ConfirmTradingNameView],
    mockAuditingService
  )

  "Calling the show action in ConfirmTradingNameController" when {

    "there is a trading name in session" should {

      "show the Confirm TradingName page" in {
        mockIndividualAuthorised()
        val result = controller.show(requestWithTradingName)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't a trading name in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.show(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to enter a new trading name" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.show(requestWithTradingName)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

  }

  "Calling the updateTradingName() action in ConfirmTradingNameController" when {

    "there is a trading name in session" when {

      "the trading name has been updated successfully" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateTradingName(vrn, testTradingName)(Future(Right(UpdateOrganisationDetailsSuccess("success"))))
          controller.updateTradingName()(requestWithTradingName)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "audit the trading name change event" in {
          verify(mockAuditingService).audit(any(), any())(any(), any())
        }

        "redirect to the trading name changed success page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.tradingName().url)
        }
      }

      "there was a conflict returned when trying to update the trading name" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateTradingName(vrn, testTradingName)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          controller.updateTradingName()(requestWithTradingName)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the manage-vat overview page" in {
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the trading name" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateTradingName(vrn, testTradingName)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify TradingName"))))
          controller.updateTradingName()(requestWithTradingName)
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show the internal server error page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a trading name in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.updateTradingName()(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture trading name page" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateTradingName()(requestWithTradingName)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
