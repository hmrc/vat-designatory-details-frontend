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

package audit.models

import assets.BaseTestConstants._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}

class ChangedTradingNameAuditModelSpec extends AnyWordSpecLike with Matchers with OptionValues {

  "ChangedTradingNameAuditModel" when {

    "the user is not an agent" should {

      val model = ChangedTradingNameAuditModel(Some(testValidationTradingName), testPrepopTradingName, vrn, isAgent = false, None, Status.OK, "formBundle")
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "currentTradingName" -> testValidationTradingName,
        "requestedTradingName" -> testPrepopTradingName,
        "etmpResponseStatusCode" -> Status.OK,
        "etmpResponseMessage" -> "formBundle"
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user is an agent" should {

      val model = ChangedTradingNameAuditModel(Some(testValidationTradingName), testPrepopTradingName, vrn, isAgent = true, Some(arn), Status.OK, "formBundle")
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> true,
        "agentReferenceNumber" -> arn,
        "vrn" -> vrn,
        "currentTradingName" -> testValidationTradingName,
        "requestedTradingName" -> testPrepopTradingName,
        "etmpResponseStatusCode" -> Status.OK,
        "etmpResponseMessage" -> "formBundle"
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user does not have a current trading name" should {

      val model = ChangedTradingNameAuditModel(None, testPrepopTradingName, vrn, isAgent = false, None, Status.OK, "formBundle")
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "requestedTradingName" -> testPrepopTradingName,
        "etmpResponseStatusCode" -> Status.OK,
        "etmpResponseMessage" -> "formBundle"
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }
  }
}
