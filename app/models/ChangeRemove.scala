/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import play.api.libs.json._

sealed trait ChangeRemove {
  val value: Boolean
}

object ChangeRemove {

  val id = "isChange"

  implicit val writes: Writes[ChangeRemove] = Writes {
    isChange => Json.obj(id -> isChange.value)
  }

  implicit val reads: Reads[ChangeRemove] = for {
    status <- (__ \ id).read[Boolean].map {
      case true => Change
      case _ => Remove
    }
  } yield status

  implicit val format: Format[ChangeRemove] = Format(reads, writes)
}

object Change extends ChangeRemove {
  override def toString: String = "change"
  override val value: Boolean = true
}

object Remove extends ChangeRemove {
  override def toString: String = "remove"
  override val value: Boolean = false
}
