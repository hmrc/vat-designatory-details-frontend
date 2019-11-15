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

package models.customerInformation

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class UpdateOrganisationDetails(organisationName: Option[String],
                                     individual: Option[String],
                                     tradingName: Option[String],
                                     capacitorEmail: Option[String])

object UpdateOrganisationDetails {

  private val organisationNamePath = JsPath \ "organisationName"
  private val individualPath = JsPath \ "individual"
  private val tradingNamePath = JsPath \ "tradingName"
  private val capacitorEmailPath = JsPath \ "transactorOrCapacitorEmail"

  implicit val writes: Writes[UpdateOrganisationDetails] = (
    organisationNamePath.writeNullable[String] and
    individualPath.writeNullable[String] and
    tradingNamePath.writeNullable[String] and
    capacitorEmailPath.writeNullable[String]
  )(unlift(UpdateOrganisationDetails.unapply))
}