/*
 * Copyright 2021 HM Revenue & Customs
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

import common.SessionKeys.prepopulationTradingNameKey
import forms.TradingNameForm
import helpers.SessionCookieCrumbler
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class CaptureTradingNamePageSpec extends BasePageISpec {

  val path = "/new-trading-name"
  val currentTradingName = "DT Autos"
  val newTradingName = "DTs Shiny Autos"

  "Calling the Capture trading name (.show) route" when {

    def show: WSResponse = get(path, formatInflightChange(Some("false")) ++
      formatValidationTradingName(Some("ABC Trading")) ++ insolvencyValue)

    "the user is authenticated" should {

      "load successfully" in {

        given.user.isAuthenticated

        val result = show

        result should have(
          httpStatus(Status.OK),
          pageTitle(generateDocumentTitle("captureNewTradingName.title"))
        )
      }
    }
  }

  "Calling the Capture trading name (.submit) route" when {

    def submit(data: String): WSResponse = post(path,
      formatValidationTradingName(Some(currentTradingName)) ++ formatInflightChange(Some("false")) ++ insolvencyValue
    )(toFormData(TradingNameForm.tradingNameForm(currentTradingName), data))

    "the user is authenticated" when {

      "a valid trading name is submitted" should {

        "redirect to the the Confirm trading name page" in {

          given.user.isAuthenticated

          val result = submit(newTradingName)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.businessTradingName.routes.CheckYourAnswersController.showTradingName.url)
          )
        }

        "add the existing trading name to the session" in {

          given.user.isAuthenticated

          val result = submit(newTradingName)

          SessionCookieCrumbler.getSessionMap(result).get(prepopulationTradingNameKey) shouldBe Some(newTradingName)
        }
      }
    }
  }
}
