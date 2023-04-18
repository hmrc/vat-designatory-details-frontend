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

package audit.models

import play.api.libs.json.{Format, JsValue, Json}
import utils.JsonObjectSugar

case class ChangedTradingNameAuditModel(currentTradingName: Option[String],
                                        requestedTradingName: String,
                                        vrn: String,
                                        isAgent: Boolean,
                                        agentReferenceNumber: Option[String],
                                        etmpResponseStatusCode: Int,
                                        etmpResponseMessage: String) extends AuditModel with JsonObjectSugar {

  override val transactionName: String = "change-trading-name"
  override val auditType: String = "ChangeTradingNameEnd"
  override val detail: JsValue = jsonObjNoNulls(
    "currentTradingName" -> currentTradingName,
    "requestedTradingName" -> requestedTradingName,
    "vrn" -> vrn,
    "isAgent" -> isAgent,
    "agentReferenceNumber" -> agentReferenceNumber,
    "etmpResponseStatusCode" -> etmpResponseStatusCode,
    "etmpResponseMessage" -> etmpResponseMessage
  )
}

object ChangedTradingNameAuditModel {
  implicit val format: Format[ChangedTradingNameAuditModel] = Json.format[ChangedTradingNameAuditModel]
}
