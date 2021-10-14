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

package controllers

import controllers.predicates.inflight.{InFlightPredicateComponents, InFlightPredicate}
import controllers.predicates.{AuthPredicate, AuthPredicateComponents}
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController


abstract class BaseController(implicit val mcc: MessagesControllerComponents,
                              authComps: AuthPredicateComponents,
                              inFlightComps: InFlightPredicateComponents) extends FrontendController(mcc) with I18nSupport {

  val authPredicate = new AuthPredicate(authComps)

  val routePrefix = "/vat-through-software/account/designatory"

  val inFlightTradingNamePredicate = new InFlightPredicate(
    inFlightComps, routePrefix + controllers.tradingName.routes.WhatToDoController.show.url, businessNameJourney = false
  )

  val businessNameAccessPredicate = new InFlightPredicate(
    inFlightComps, routePrefix + controllers.businessName.routes.CaptureBusinessNameController.show.url, businessNameJourney = true
  )
}
