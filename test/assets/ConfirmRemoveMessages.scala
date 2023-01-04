/*
 * Copyright 2023 HM Revenue & Customs
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

package assets

object ConfirmRemoveMessages {

  val title = "Do you want us to remove your trading name? - Manage your VAT account - GOV.UK"
  def heading(tradingName: String): String =
    s"Do you want us to remove $tradingName as the trading name?"
  val confirm = "Confirm and continue"
  val yes = "Yes"
  val no = "No"
  val back = "Back"
  val errorPrefix = "Error: "
  val errorTitle = errorPrefix + title
  val errorHeading = "There is a problem"
  val errorLinkText = "Select yes if you want us to remove the current trading name"
  val errorText = errorPrefix + errorLinkText

}
