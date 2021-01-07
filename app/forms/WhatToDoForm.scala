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

package forms

import models.{ChangeRemove, Change, Remove}
import play.api.data.{Form, FormError}
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter

object WhatToDoForm {

  val changeRemove: String = "change_remove"

  val change: String = "change"

  val remove: String = "remove"

  def formatter: Formatter[ChangeRemove] = new Formatter[ChangeRemove] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ChangeRemove] = {
      data.get(key) match {
        case Some(`change`) => Right(Change)
        case Some(`remove`) => Right(Remove)
        case _ => Left(Seq(FormError(key, "whatToDo.error")))
      }
    }

    override def unbind(key: String, value: ChangeRemove): Map[String, String] = {
      val stringValue = value match {
        case Change => change
        case Remove => remove
      }

      Map(key -> stringValue)
    }
  }

  val whatToDoForm: Form[ChangeRemove] = Form(
    single(
      changeRemove -> of(formatter)
    )
  )
}
