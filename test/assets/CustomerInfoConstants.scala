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

import common.ContactPreference
import models.ChangeIndicators
import models.customerInformation._
import models.errors.ErrorModel
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}

object CustomerInfoConstants {

  val organisationDetailsJsonMin: JsObject = Json.obj()

  val pendingTradingNameJson: JsObject = Json.obj(
    "tradingName" -> "New trading name"
  )

  val pendingTradingNameModel: PendingChanges = PendingChanges(Some("New trading name"))
  val changeIndicatorsModel: ChangeIndicators = ChangeIndicators(true)

  val minCustomerInfoModel: CustomerInformation = CustomerInformation(None, None, None, None, None, None, None, None, None, false, None, None)
  val minCustomerInfoJson: JsObject = Json.obj(
    "customerDetails" -> Json.obj(
      "isInsolvent" -> false
    ))

  val fullCustomerInfoModel: CustomerInformation = CustomerInformation(
    pendingChanges = Some(pendingTradingNameModel),
    firstName = Some("Pepsi"),
    lastName = Some("Mac"),
    organisationName = Some("PepsiMac Ltd"),
    tradingName = Some("PepsiMac"),
    contactPreference = Some(ContactPreference.digital),
    changeIndicators = Some(changeIndicatorsModel),
    nameIsReadOnly = Some(false),
    partyType = Some("1"),
    isInsolvent = false,
    continueToTrade = Some(true),
    insolvencyType = Some("01")
  )

  val customerInfoNoPending: CustomerInformation = fullCustomerInfoModel.copy(pendingChanges = None, changeIndicators = Some(ChangeIndicators(false)))

  val customerInfoNoBusinessName: CustomerInformation = fullCustomerInfoModel.copy(organisationName = None)

  val fullCustomerInfoModelSameTradingName: CustomerInformation = fullCustomerInfoModel.copy(
    tradingName = Some("New trading name")
  )

  val fullCustomerInfoJson: JsObject = Json.obj(
    "pendingChanges" -> pendingTradingNameJson,
    "customerDetails" -> Json.obj(
      "firstName" -> "Pepsi",
      "lastName" -> "Mac",
      "organisationName" -> "PepsiMac Ltd",
      "tradingName" -> "PepsiMac",
      "nameIsReadOnly" -> false,
      "isInsolvent" -> false,
      "continueToTrade" -> Some(true),
      "insolvencyType" -> Some("01")
    ),
    "commsPreference" -> "DIGITAL",
    "changeIndicators" -> Some(changeIndicatorsModel),
    "partyType" -> "1"
  )

  val customerInfoInsolvent: CustomerInformation = fullCustomerInfoModel.copy(isInsolvent = true, continueToTrade = Some(false))

  val updateTradingNameModel: UpdateTradingName =
    UpdateTradingName(Some("PepsiMac"), Some("myAgentEmail@email.com"))
  val updateTradingNameModelMin: UpdateTradingName =
    UpdateTradingName(None, None)

  val updateTradingNameJson: JsObject = Json.obj(
    "tradingName" -> "PepsiMac", "transactorOrCapacitorEmail" -> "myAgentEmail@email.com"
  )
  val updateTradingNameJsonMin: JsObject = Json.obj()

  val updateBusinessNameModel: UpdateBusinessName = UpdateBusinessName("Just a Business", Some("bestAgentEva@email.com"))

  val updateBusinessNameJson: JsObject = Json.obj(
    "organisationName" -> "Just a Business", "transactorOrCapacitorEmail" -> "bestAgentEva@email.com"
  )

  val updateBusinessNameModelMin: UpdateBusinessName = UpdateBusinessName("Just a Business", None)

  val updateBusinessNameJsonMin: JsObject = Json.obj("organisationName" -> "Just a Business")

  val invalidJsonError: ErrorModel = ErrorModel(Status.INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON.")
}
