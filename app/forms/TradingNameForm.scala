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

import play.api.data.Form
import play.api.data.Forms._
import utils.StopOnFirstFail
import utils.StopOnFirstFail.constraint

object TradingNameForm {

  val maxLength: Int = 160
  val tradingNameRegex = """^[-A-Za-z0-9 '’‘()[\\]{}<>!«»\"?\/\\\\+=%#*&$€£_@¥.,:;]{1,160}$"""

  def tradingNameForm(tradingName: String): Form[String] = Form(
    "trading-name" -> text.verifying(
      StopOnFirstFail(
        constraint[String]("captureTradingName.error.empty", _.length != 0),
        constraint[String]("captureTradingName.error.notChanged", _.toLowerCase != tradingName.toLowerCase),
        constraint[String]("captureTradingName.error.exceedsMaxLength", _.length <= maxLength),
        constraint[String]("captureTradingName.error.containsSpecialCharacters", _.matches(tradingNameRegex))
      )
    )
  )
}
