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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.WireMockMethods
import models.ChangeIndicators
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsObject, Json}

object VatSubscriptionStub extends WireMockMethods {

  private val getCustomerInfoUri: String = "/vat-subscription/([0-9]+)/full-information"

  private val updateBusinessNameUri: String = "/vat-subscription/([0-9]+)/business-name"

  def stubCustomerInfo: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = OK, body = customerInfoJson)
  }

  def stubCustomerInfoInvalidJson: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = OK, body = emptyCustomerInfo)
  }

  def stubCustomerInfoError: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj("fail" -> "nope"))
  }

  def stubUpdateBusinessName: StubMapping = {
    when(method = PUT, uri = updateBusinessNameUri)
      .thenReturn(status = OK, body = Json.obj("formBundle" -> "success"))
  }

  def stubUpdateBusinessNameError: StubMapping = {
    when(method = PUT, uri = updateBusinessNameUri)
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj("bad" -> "things"))
  }

  val currentTradingName = "Current Trading Name"
  val changeIndicatorsModel: ChangeIndicators = ChangeIndicators(true)

  val customerInfoJson: JsObject = Json.obj(
    "customerDetails" -> Json.obj(
      "firstName" -> "Dave",
      "lastName" -> "Taylor",
      "organisationName" -> "D Taylor's Cars",
      "tradingName" -> "DT Autos",
      "nameIsReadOnly" -> false,
      "isInsolvent" -> false,
      "continueToTrade" -> true
    ),
    "commsPreference" -> "DIGITAL",
    "changeIndicators" -> changeIndicatorsModel,
    "partyType" -> "1"
  )

  val emptyCustomerInfo: JsObject = Json.obj("xxx" -> "xxx")
}
