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

import audit.AuditingService
import controllers.ControllerBaseSpec
import play.api.test.Helpers._
import views.html.businessTradingName.CheckYourAnswersView


class CheckYourAnswersControllerSpec extends ControllerBaseSpec {

  implicit val auditingService: AuditingService = inject[AuditingService]

  val controller = new CheckYourAnswersController(
    inject[CheckYourAnswersView]
  )

  "Calling the show action in CheckYourAnswersController" when {

    "there is a trading name in session" should {

      "show the Check your answer page" in {
        mockIndividualAuthorised()
        val result = controller.show(requestWithTradingName)

        status(result) shouldBe OK
      }
    }

    "there isn't a trading name in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.show(request)
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
        val result = controller.show(requestWithTradingName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "Calling updateTradingName() in CheckYourAnswersController" when {

    "there is a trading name in session" when {

      "the trading name has been updated successfully" should {

        lazy val result = {
          controller.updateTradingName()(requestWithTradingName)
        }

        "return 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the trading name changed success page" in {
          redirectLocation(result) shouldBe Some(controllers.businessTradingName.routes.ChangeSuccessController.tradingName().url)
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
}
