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

package pages.businessTradingName

import common.SessionKeys
import common.SessionKeys.{businessNameAccessPermittedKey, validationBusinessNameKey, prepopulationBusinessNameKey, prepopulationTradingNameKey}
import helpers.SessionCookieCrumbler
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class CheckYourAnswersPageSpec extends BasePageISpec {

  val updateTradingNamePath = "/confirm-new-trading-name"
  val updateBusinessNamePath = "/confirm-new-business-name"
  val newTradingName = "New Trading Name"
  val newBusinessName = "New Business Name"

  "Calling the Check your answers (.show trading name) route" when {

    def show: WSResponse = get(updateTradingNamePath, Map(prepopulationTradingNameKey -> newTradingName)
      ++ formatInflightChange(Some("false")) ++ insolvencyValue)

    "the user is a authenticated" when {

      "there is a new trading name in session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("checkYourAnswers.heading"))
          )
        }
      }
    }
  }

  "Calling the Check your answers (.updateTradingName) route" when {

    def update: WSResponse = post(updateTradingNamePath, Map(
      prepopulationTradingNameKey -> newTradingName) ++ formatInflightChange(Some("false")) ++ insolvencyValue
    )(Map())

    "the user is authenticated" when {

      "the vat subscription service successfully updates the trading name" should {

        "redirect to the Change Success Controller" in {

          given.user.isAuthenticated

          When("The update trading name route is called")

          And("a successful trading name update response is stubbed")
          VatSubscriptionStub.stubUpdateTradingName

          val result = update

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.routes.ChangeSuccessController.tradingName.url)
          )
        }

        "add the tradingNameChangeSuccessful and inFlightOrgDetailsKey to session" in {

          given.user.isAuthenticated

          When("The update trading name route is called")

          And("a successful trading name update response is stubbed")
          VatSubscriptionStub.stubUpdateTradingName

          val result = update

          SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.tradingNameChangeSuccessful) shouldBe Some("true")
          SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.inFlightOrgDetailsKey) shouldBe Some("true")
        }
      }
    }
  }

  "Calling the Check your answers (.show business name) route" when {

    def show: WSResponse = get(updateBusinessNamePath, Map(prepopulationBusinessNameKey -> newBusinessName)
      ++ formatInflightChange(Some("false"))
      ++ formatBusinessNameAccess
      ++ insolvencyValue)

    "the user is a authenticated" when {

      "there is a new business name in session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("checkYourAnswers.heading"))
          )
        }
      }
    }
  }

  "Calling the Check your answers (.updateBusinessName) route" when {

    def update: WSResponse = post(updateBusinessNamePath, Map(
      validationBusinessNameKey -> "Business Name", prepopulationBusinessNameKey -> newBusinessName, businessNameAccessPermittedKey -> "true"
    ) ++ formatInflightChange(Some("false")) ++ insolvencyValue)(Map())

    "the user is a authenticated" when {

      "the vat subscription service successfully updates the business name" should {

        "redirect to the Change Success Controller" in {

          given.user.isAuthenticated

          When("The update business name route is called")

          And("a successful business name update response is stubbed")
          VatSubscriptionStub.stubUpdateBusinessName

          val result = update

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.routes.ChangeSuccessController.businessName.url)
          )
        }

        "add the businessNameChangeSuccessful and inFlightOrgDetailsKey to session" in {

          given.user.isAuthenticated

          When("The update business name route is called")

          And("a successful business name update response is stubbed")
          VatSubscriptionStub.stubUpdateBusinessName

          val result = update

          SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.businessNameChangeSuccessful) shouldBe Some("true")
          SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.inFlightOrgDetailsKey) shouldBe Some("true")
        }
      }
    }
  }
}
