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

package forms

import forms.TradingNameForm._
import play.api.data.FormError
import utils.TestUtil

class TradingNameFormSpec extends TestUtil {

  "The trading name form" should {

    val maxLengthErrorMessage: String = "captureTradingName.error.exceedsMaxLength"
    val noEntryErrorMessage: String = "captureTradingName.error.empty"
    val unchangedErrorMessage: String = "captureTradingName.error.notChanged"

    val testTradingName = "Valid Trading Name"

    "validate that testTradingName is valid" in {
      val actual = tradingNameForm("").bind(Map("trading-name" -> testTradingName))
      actual.value shouldBe Some(testTradingName)
    }

    "validate that trading name does not exceed max length" in {
      val exceed = tradingNameForm("").bind(Map("trading-name" -> ("a" * (maxLength + 1)))).errors
      exceed should contain(FormError("trading-name", maxLengthErrorMessage))
      exceed.seq.size shouldBe 1
    }

    "validate that trading name allows max length" in {
      val errors = tradingNameForm("").bind(Map("trading-name" -> ("a" * maxLength))).errors
      errors should not contain FormError("trading-name", maxLengthErrorMessage)
    }

    "validate the trading name is not empty" in {
      val errors = tradingNameForm("").bind(Map("trading-name" -> "")).errors
      errors should contain(FormError("trading-name", noEntryErrorMessage))
    }

    "validate the trading name has been changed" in {
      val errors = tradingNameForm(testTradingName).bind(Map("trading-name" -> testTradingName)).errors
      errors should contain(FormError("trading-name", unchangedErrorMessage))
    }
  }
}
