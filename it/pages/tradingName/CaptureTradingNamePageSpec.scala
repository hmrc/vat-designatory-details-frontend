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

package pages.tradingName

import common.SessionKeys.{prepopulationTradingNameKey, validationTradingNameKey}
import forms.TradingNameForm
import helpers.SessionCookieCrumbler
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class CaptureTradingNamePageSpec extends BasePageISpec {

  val path = "/new-trading-name"
  val currentTradingName = "DT Autos"
  val newTradingName = "DTs Shiny Autos"

  "Calling the Capture trading name (.show) route" when {

    def show: WSResponse = get(path, formatInflightChange(Some("false")))

    "the user is authenticated" when {

      "a success response with a trading name is received from Vat Subscription" should {

        "load successfully" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("captureTradingName.title"))
          )
        }

        "add the existing trading name to session" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = show

          SessionCookieCrumbler.getSessionMap(result).get(validationTradingNameKey) shouldBe Some(currentTradingName)
        }
      }
    }
  }

  "Calling the Capture trading name (.submit) route" when {

    def submit(data: String): WSResponse = post(path,
      Map(validationTradingNameKey -> currentTradingName)
        ++ formatInflightChange(Some("false"))
    )(toFormData(TradingNameForm.tradingNameForm(currentTradingName), data))

    "the user is authenticated" when {

      "a valid trading name is submitted" should {

        "redirect to the the Confirm trading name page" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = submit(newTradingName)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.tradingName.routes.CheckYourAnswersController.show().url)
          )
        }

        "add the existing trading name to the session" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = submit(newTradingName)

          SessionCookieCrumbler.getSessionMap(result).get(prepopulationTradingNameKey) shouldBe Some(newTradingName)
        }
      }
    }
  }
}
