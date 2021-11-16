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

package models.customerInformation

import models.ChangeIndicators
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CustomerInformation(pendingChanges: Option[PendingChanges],
                               firstName: Option[String],
                               lastName: Option[String],
                               organisationName: Option[String],
                               tradingName: Option[String],
                               contactPreference: Option[String],
                               changeIndicators: Option[ChangeIndicators],
                               nameIsReadOnly: Option[Boolean],
                               partyType: Option[String],
                               isInsolvent: Boolean,
                               continueToTrade: Option[Boolean],
                               insolvencyType: Option[String]) {

  val allowedInsolvencyTypes: Seq[String] = Seq("07", "12", "13", "14")
  val blockedInsolvencyTypes: Seq[String] = Seq("08", "09", "10", "15")

  val pendingTradingName: Option[String] = pendingChanges.flatMap(_.tradingName)

  val sameTradingName: Boolean = pendingTradingName == tradingName

  def entityName: Option[String] =
    (firstName, lastName, tradingName, organisationName) match {
      case (Some(first), Some(last), None, None) => Some(s"$first $last")
      case (None, None, None, orgName) => orgName
      case _ => tradingName
    }

  val isValidPartyType: Boolean =
    Seq("Z1", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "50",
      "51", "52", "53", "54", "55", "58", "59", "60", "61", "62", "63").contains(partyType.getOrElse(""))

  val isInsolventWithoutAccess: Boolean = {
    if (isInsolvent) {
      insolvencyType match {
        case Some(iType) if allowedInsolvencyTypes.contains(iType) => false
        case Some(iType) if blockedInsolvencyTypes.contains(iType) => true
        case _ => !continueToTrade.getOrElse(true)
      }
    } else {
      false
    }
  }
}

object CustomerInformation {

  private val pendingChangesPath = JsPath \ "pendingChanges"
  private val firstNamePath = JsPath \ "customerDetails" \ "firstName"
  private val lastNamePath = JsPath \ "customerDetails" \ "lastName"
  private val organisationNamePath = JsPath \ "customerDetails" \ "organisationName"
  private val tradingNamePath = JsPath \ "customerDetails" \ "tradingName"
  private val contactPreferencePath = JsPath \ "commsPreference"
  private val changeIndicatorsPath = JsPath \ "changeIndicators"
  private val nameIsReadOnlyPath = JsPath \ "customerDetails" \ "nameIsReadOnly"
  private val partyTypePath = JsPath \ "partyType"
  private val isInsolventPath = JsPath \ "customerDetails" \ "isInsolvent"
  private val continueToTradePath = JsPath \ "customerDetails" \ "continueToTrade"
  private val insolvencyTypePath = JsPath \ "customerDetails" \ "insolvencyType"

  implicit val reads: Reads[CustomerInformation] = (
    pendingChangesPath.readNullable[PendingChanges] and
    firstNamePath.readNullable[String] and
    lastNamePath.readNullable[String] and
    organisationNamePath.readNullable[String] and
    tradingNamePath.readNullable[String] and
    contactPreferencePath.readNullable[String] and
    changeIndicatorsPath.readNullable[ChangeIndicators] and
    nameIsReadOnlyPath.readNullable[Boolean] and
    partyTypePath.readNullable[String] and
    isInsolventPath.read[Boolean] and
    continueToTradePath.readNullable[Boolean] and
    insolvencyTypePath.readNullable[String]
  )(CustomerInformation.apply _)
}
