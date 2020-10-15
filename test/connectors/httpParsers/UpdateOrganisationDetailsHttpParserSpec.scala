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

package connectors.httpParsers

import assets.UpdateTradingNameConstants._
import assets.CustomerInfoConstants.invalidJsonError
import connectors.httpParsers.UpdateOrganisationDetailsHttpParser.UpdateOrganisationDetailsReads._
import models.errors.ErrorModel
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

class UpdateOrganisationDetailsHttpParserSpec extends UnitSpec {

  "read" when {
    "the response status is OK" should {
      "return a updateTradingNameSuccess when the response Json can be parsed" in {
        val httpResponse = HttpResponse(Status.OK, Json.obj("formBundle" -> s"$formBundle").toString)

        read("", "", httpResponse) shouldBe Right(updateTradingNameSuccess)
      }

      "return the expected Left Error Model when the response Json cannot be parsed" in {
        val httpResponse = HttpResponse(Status.OK, Json.obj("notExpectedKey" -> s"$formBundle").toString)

        read("", "", httpResponse) shouldBe Left(invalidJsonError)
      }
    }

    "the response status is INTERNAL_SERVER_ERROR" should {
      "return the expected Left Error Model" in {
        val httpResponse: HttpResponse = HttpResponse(
          INTERNAL_SERVER_ERROR, ""
        )
        read("", "", httpResponse) shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, httpResponse.body))
      }
    }
  }
}
