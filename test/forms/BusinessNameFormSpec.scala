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

package forms

import forms.BusinessNameForm._
import play.api.data.FormError
import utils.TestUtil

class BusinessNameFormSpec extends TestUtil {

  "The business name form" should {

    val maxLengthErrorMessage: String = "captureBusinessName.error.exceedsMaxLength"
    val noEntryErrorMessage: String = "captureBusinessName.error.empty"
    val unchangedErrorMessage: String = "captureBusinessName.error.notChanged"
    val specialCharsErrorMessage: String = "captureBusinessName.error.containsSpecialCharacters"

    val testBusinessName = "Valid` & Business-' Name."

    "validate that testBusinessName is valid" in {
      val actual = businessNameForm("").bind(Map("business-name" -> testBusinessName))
      actual.value shouldBe Some(testBusinessName)
    }

    "validate that business name does not exceed max length" in {
      val exceed = businessNameForm("").bind(Map("business-name" -> ("a" * (maxLength + 1)))).errors
      exceed should contain(FormError("business-name", maxLengthErrorMessage))
      exceed.seq.size shouldBe 1
    }

    "validate that business name allows max length" in {
      val errors = businessNameForm("").bind(Map("business-name" -> ("a" * maxLength))).errors
      errors should not contain FormError("business-name", maxLengthErrorMessage)
    }

    "validate the business name is not empty" in {
      val errors = businessNameForm("").bind(Map("business-name" -> "")).errors
      errors should contain(FormError("business-name", noEntryErrorMessage))
    }

    "validate the business name has been changed" in {
      val errors = businessNameForm(testBusinessName).bind(Map("business-name" -> testBusinessName)).errors
      errors should contain(FormError("business-name", unchangedErrorMessage))
    }

    "validate the business name does not contain special characters" in {
      val errors = businessNameForm("" ).bind(Map("business-name" -> ("%" + testBusinessName))).errors
      errors should contain(FormError("business-name", specialCharsErrorMessage))
    }
  }

}
