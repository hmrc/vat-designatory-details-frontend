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

package pages.businessTradingName

import common.SessionKeys.{prepopulationBusinessNameKey, prepopulationTradingNameKey}
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class CheckYourAnswersPageSpec extends BasePageISpec {

  val tradingNamePath = "/confirm-new-trading-name"
  val businessNamePath = "/confirm-new-business-name"
  val newTradingName = "New Trading Name"
  val newBusinessName = "New Business Name"

  "Calling the Check your answers (.show trading name) route" when {

    def show: WSResponse = get(tradingNamePath, Map(prepopulationTradingNameKey -> newTradingName)
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
  "Calling the Check your answers (.show business name) route" when {

    def show: WSResponse = get(businessNamePath, Map(prepopulationBusinessNameKey -> newTradingName)
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
}
