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

package pages.tradingName

import forms.WhatToDoForm.{change, changeRemove, remove}
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class WhatToDoPageSpec extends BasePageISpec {

  val path = "/change-remove-trading-name"

  "Calling the WhatToDo (.show) route" when {

    def show: WSResponse = get(path, formatInflightChange(Some("false")) ++ insolvencyValue)

    "the user is authenticated" when {

      "a successful response is returned from VAT Subscription" should {

        "load successfully" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("whatToDo.title"))
          )
        }
      }
    }
  }

  "Calling the WhatToDo (.submit) route" when {

    def submit(data: String): WSResponse = post(path,
      formatInflightChange(Some("false")) ++ formatValidationTradingName(Some("ABC Trading")))(
      Map(changeRemove -> Seq(data))
    )

    "the user is authenticated" when {

      "'Change' is submitted" should {

        "redirect to the Capture Trading Name page" in {

          given.user.isAuthenticated

          val result = submit(change)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.tradingName.routes.CaptureTradingNameController.show().url)
          )
        }
      }

      "'Remove' is submitted" should {

        "redirect to the Confirm Remove Trading Name page" in {

          given.user.isAuthenticated

          val result = submit(remove)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.tradingName.routes.ConfirmRemoveTradingNameController.show().url)
          )
        }
      }
    }
  }
}
