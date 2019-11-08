/*
 * Copyright 2019 HM Revenue & Customs
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

import models.customerInformation._
import models.errors.ErrorModel
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}

object CustomerInfoConstants {

  val organisationDetailsModel = OrganisationDetails(
    Some("Org name"),
    Some("Individual"),
    Some("Trading name")
  )

  val organisationDetailsJson: JsObject = Json.obj(
    "organisationName" -> "Org name",
    "individual" -> "Individual",
    "tradingName" -> "Trading name"
  )

  val organisationDetailsModelMin = OrganisationDetails(
    None,
    None,
    None
  )

  val organisationDetailsJsonMin: JsObject = Json.obj()

  val pendingTradingNameJson: JsObject = Json.obj(
    "organisationDetails" -> Json.obj(
        "organisationName" -> "Org name",
        "individual" -> "Individual",
        "tradingName" -> "Pending trading name"
    )
  )

  val pendingTradingNameModel: PendingChanges = PendingChanges(
    Some(organisationDetailsModel.copy(tradingName = Some("Pending trading name"))))

  val minCustomerInfoModel = CustomerInformation(None, None, None, None, None)
  val minCustomerInfoJson: JsObject = Json.obj()

  val fullCustomerInfoModel = CustomerInformation(
    pendingChanges = Some(PendingChanges(Some(organisationDetailsModel))),
    firstName = Some("Pepsi"),
    lastName = Some("Mac"),
    organisationName = Some("PepsiMac Ltd"),
    tradingName = Some("PepsiMac")
  )

  val fullCustomerInfoModelSameTradingName = fullCustomerInfoModel.copy(
    tradingName = Some("Trading name")
  )

  val customerInfoPendingTradingNameModel: CustomerInformation = CustomerInformation(
    pendingChanges = Some(pendingTradingNameModel),
    firstName = Some("Pepsi"),
    lastName = Some("Mac"),
    organisationName = Some("PepsiMac Ltd"),
    tradingName = Some("PepsiMac")
  )

  val customerInfoPendingTradingNameJson: JsObject = Json.obj(
    "pendingChanges" -> pendingTradingNameJson,
    "customerDetails" -> Json.obj(
      "firstName" -> "Pepsi",
      "lastName" -> "Mac",
      "organisationName" -> "PepsiMac Ltd",
      "tradingName" -> "PepsiMac"
    )
  )

  val invalidJsonError = ErrorModel(Status.INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON.")
}
