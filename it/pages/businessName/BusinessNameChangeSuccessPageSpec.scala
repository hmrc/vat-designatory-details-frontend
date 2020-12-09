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

package pages.businessName

import common.SessionKeys.businessNameChangeSuccessful
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class BusinessNameChangeSuccessPageSpec extends BasePageISpec {

  val path = "/business-name-confirmation"

  "Calling the business name change success route" when {

    def show: WSResponse = get(path, Map(businessNameChangeSuccessful -> "true"))

    "the user is authenticated" when {

      "there is a change successful indicator in session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("businessNameChangeSuccess.title"))
          )
        }
      }
    }
  }

}
