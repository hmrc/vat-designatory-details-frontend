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
                               continueToTrade: Option[Boolean]) {

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

  val isInsolventWithoutAccess: Boolean = continueToTrade match {
    case Some(false) => isInsolvent
    case _ => false
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
  private val isInsolventPath = JsPath \ "isInsolvent"
  private val continueToTradePath = JsPath \ "continueToTrade"

  implicit val reads: Reads[CustomerInformation] = (
    pendingChangesPath.readNullable[PendingChanges].orElse(Reads.pure(None)) and
    firstNamePath.readNullable[String].orElse(Reads.pure(None)) and
    lastNamePath.readNullable[String].orElse(Reads.pure(None)) and
    organisationNamePath.readNullable[String].orElse(Reads.pure(None)) and
    tradingNamePath.readNullable[String].orElse(Reads.pure(None)) and
    contactPreferencePath.readNullable[String].orElse(Reads.pure(None)) and
    changeIndicatorsPath.readNullable[ChangeIndicators].orElse(Reads.pure(None)) and
    nameIsReadOnlyPath.readNullable[Boolean].orElse(Reads.pure(None)) and
    partyTypePath.readNullable[String].orElse(Reads.pure(None)) and
    isInsolventPath.read[Boolean].orElse(Reads.pure(false)) and
    continueToTradePath.readNullable[Boolean].orElse(Reads.pure(None))
  )(CustomerInformation.apply _)
}
