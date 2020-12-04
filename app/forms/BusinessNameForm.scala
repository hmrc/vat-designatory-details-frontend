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

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.hmrc.play.mappers.StopOnFirstFail.constraint

object BusinessNameForm {

  val maxLength: Int = 160

  def businessNameForm(tradingName: String): Form[String] = Form(
    "trading-name" -> text.verifying(
      StopOnFirstFail(
        constraint[String]("captureBusinessName.error.empty", _.length != 0),
        constraint[String]("captureBusinessName.error.notChanged", _.toLowerCase != tradingName.toLowerCase),
        constraint[String]("captureBusinessName.error.exceedsMaxLength", _.length <= maxLength)
      )
    )
  )

}
