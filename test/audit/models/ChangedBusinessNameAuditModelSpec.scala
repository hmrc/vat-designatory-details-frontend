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

package audit.models

import assets.BaseTestConstants._
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec

class ChangedBusinessNameAuditModelSpec extends UnitSpec {

  "ChangedBusinessNameAuditModel" when {

    "the user is not an agent" should {

      val model = ChangedBusinessNameAuditModel(testValidationBusinessName, testPrepopBusinessName, vrn, isAgent = false, None, Status.OK, "formBundle")
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "currentBusinessName" -> testValidationBusinessName,
        "requestedBusinessName" -> testPrepopBusinessName,
        "etmpResponseStatusCode" -> Status.OK,
        "etmpResponseMessage" -> "formBundle"
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user is an agent" should {

      val model = ChangedBusinessNameAuditModel(
        testValidationBusinessName, testPrepopBusinessName, vrn, isAgent = true, Some(arn), Status.OK, "formBundle"
      )
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> true,
        "agentReferenceNumber" -> arn,
        "vrn" -> vrn,
        "currentBusinessName" -> testValidationBusinessName,
        "requestedBusinessName" -> testPrepopBusinessName,
        "etmpResponseStatusCode" -> Status.OK,
        "etmpResponseMessage" -> "formBundle"
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }
  }
}