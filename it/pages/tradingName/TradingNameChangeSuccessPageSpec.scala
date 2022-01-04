/*
 * Copyright 2022 HM Revenue & Customs
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

import common.SessionKeys.{prepopulationTradingNameKey, tradingNameChangeSuccessful, validationTradingNameKey}
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class TradingNameChangeSuccessPageSpec extends BasePageISpec {

  val path = "/trading-name-confirmation"
  val newTradingName = "New Trading Name"
  val oldTradingName = "Old Trading Name"

  "Calling the trading name change success route" when {

    def show: WSResponse = get(path, Map(prepopulationTradingNameKey -> newTradingName,
      validationTradingNameKey -> oldTradingName, tradingNameChangeSuccessful -> "true") ++ insolvencyValue)

    "the user is authenticated" when {

      "there is a new trading name and change indicator in session" should {

        "load successfully" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("tradingNameChangeSuccess.title.change"))
          )
        }
      }
    }
  }
}
