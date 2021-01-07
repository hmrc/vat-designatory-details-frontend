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

  val minCustomerInfoModel: CustomerInformation = CustomerInformation(None, None, None, None, None, None, None, None, None)
  val minCustomerInfoJson: JsObject = Json.obj()

  val fullCustomerInfoModel: CustomerInformation = CustomerInformation(
    pendingChanges = Some(pendingTradingNameModel),
    firstName = Some("Pepsi"),
    lastName = Some("Mac"),
    organisationName = Some("PepsiMac Ltd"),
    tradingName = Some("PepsiMac"),
    contactPreference = Some(ContactPreference.digital),
    changeIndicators = Some(changeIndicatorsModel),
    nameIsReadOnly = Some(false),
    partyType = Some("1")
  )

  val customerInfoNoPending: CustomerInformation = fullCustomerInfoModel.copy(pendingChanges = None, changeIndicators = Some(ChangeIndicators(false)))

  val customerInfoNoBusinessName = fullCustomerInfoModel.copy(organisationName = None)

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
      "nameIsReadOnly" -> false
    ),
    "commsPreference" -> "DIGITAL",
    "changeIndicators" -> Some(changeIndicatorsModel),
    "partyType" -> "1"
  )

  val updateOrganisationDetailsModel: UpdateOrganisationDetails =
    UpdateOrganisationDetails("PepsiMac", Some("myAgentEmail@email.com"))
  val updateOrganisationDetailsModelMin: UpdateOrganisationDetails =
    UpdateOrganisationDetails("PepsiMac", None)

  val updateOrganisationDetailsJson: JsObject = Json.obj(
    "tradingName" -> "PepsiMac", "transactorOrCapacitorEmail" -> "myAgentEmail@email.com"
  )
  val updateOrganisationDetailsJsonMin: JsObject = Json.obj("tradingName" -> "PepsiMac")

  val invalidJsonError: ErrorModel = ErrorModel(Status.INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON.")
}
