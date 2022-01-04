/*
 * Copyright 2022 HM Revenue & Customs
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

case class ChangedBusinessNameAuditModel(currentBusinessName: String,
                                         requestedBusinessName: String,
                                         vrn: String,
                                         isAgent: Boolean,
                                         agentReferenceNumber: Option[String],
                                         etmpResponseStatusCode: Int,
                                         etmpResponseMessage: String) extends AuditModel {

  override val transactionName: String = "change-business-name"
  override val auditType: String = "ChangeBusinessName"
  override val detail: JsValue = Json.toJson(this)

}

object ChangedBusinessNameAuditModel {
  implicit val format: Format[ChangedBusinessNameAuditModel] = Json.format[ChangedBusinessNameAuditModel]
}
