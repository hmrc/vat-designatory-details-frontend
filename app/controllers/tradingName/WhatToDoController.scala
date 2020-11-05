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

package controllers.tradingName

import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.WhatToDoForm.whatToDoForm
import javax.inject.{Inject, Singleton}
import models.{Change, Remove}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.tradingName.WhatToDoView

@Singleton
class WhatToDoController @Inject()(whatToDoView: WhatToDoView)
                                  (implicit val appConfig: AppConfig,
                                   mcc: MessagesControllerComponents,
                                   authComps: AuthPredicateComponents,
                                   inFlightComps: InFlightPredicateComponents) extends BaseController{

  def show: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>
    Ok(whatToDoView(whatToDoForm("whatToDo.error")))
  }

  def submit: Action[AnyContent] = (authPredicate andThen inFlightTradingNamePredicate) { implicit user =>
    whatToDoForm("whatToDo.error").bindFromRequest.fold(
      errorForm => {
        BadRequest(whatToDoView(errorForm))
      },
      wantToChange => {
        wantToChange match {
          case Change => Redirect(routes.CaptureTradingNameController.show())
          case Remove => Redirect(routes.CaptureTradingNameController.show())
        }
      }
    )
  }

}
