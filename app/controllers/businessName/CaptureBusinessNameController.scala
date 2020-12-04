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

package controllers.businessName

import audit.AuditingService
import common.SessionKeys.{prepopulationTradingNameKey, validationTradingNameKey}
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.BusinessNameForm.businessNameForm
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import views.html.businessName.CaptureBusinessNameView

import scala.concurrent.Future

@Singleton
class CaptureBusinessNameController @Inject()(captureBusinessNameView: CaptureBusinessNameView)
                                             (implicit val appConfig: AppConfig,
                                              mcc: MessagesControllerComponents,
                                              authComps: AuthPredicateComponents,
                                              inFlightComps: InFlightPredicateComponents) extends BaseController {

}
