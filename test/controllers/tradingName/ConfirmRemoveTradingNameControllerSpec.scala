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

import common.SessionKeys
import controllers.ControllerBaseSpec
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.tradingName.ConfirmRemoveTradingNameView

class ConfirmRemoveTradingNameControllerSpec extends ControllerBaseSpec  {

  lazy val controller = new ConfirmRemoveTradingNameController(
    mockErrorHandler,
    mockVatSubscriptionService,
    inject[ConfirmRemoveTradingNameView]
  )

  lazy val requestWithValidationTradingNameKey: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.validationTradingNameKey -> testTradingName)

  "Calling .show in ConfirmRemoveTradingNameController" when {

    "there is a trading name in session" should {

      "return 200" in {
        val result = controller.show(requestWithValidationTradingNameKey)
        status(result) shouldBe Status.OK
      }
    }

    "there is no trading name in session" should {

      lazy val result = controller.show(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture trading name page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureTradingNameController.show().url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.show(requestWithValidationTradingNameKey)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling .removeTradingName() in ConfirmRemoveTradingNameController" when {

    "there is a validation trading name in session" should {

      lazy val result = controller.removeTradingName()(requestWithValidationTradingNameKey)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the updateTradingName() action in ConfirmTradingNameController" in {
        redirectLocation(result) shouldBe Some(routes.CheckYourAnswersController.updateTradingName().url)
      }

      "add a blank value to the prepopulation session key" in {
        session(result).get(SessionKeys.prepopulationTradingNameKey) shouldBe Some("")
      }
    }

    "there is no validation trading name in session" should {

      lazy val result = controller.removeTradingName()(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture trading name page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureTradingNameController.show().url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.removeTradingName()(requestWithValidationTradingNameKey)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
