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

package controllers.predicates

import config.{AppConfig, ErrorHandler}
import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.MessagesControllerComponents
import services.{EnrolmentsAuthService, VatSubscriptionService}
import views.html.errors.{NotSignedUpView, SessionTimeoutView, UserInsolventErrorView}
import views.html.errors.agent.UnauthorisedAgentView

import scala.concurrent.ExecutionContext

@Singleton
class AuthPredicateComponents @Inject()(val enrolmentsAuthService: EnrolmentsAuthService,
                                        val mcc: MessagesControllerComponents,
                                        val errorHandler: ErrorHandler,
                                        val vatSubscriptionService: VatSubscriptionService,
                                        val authenticateAsAgentWithClient: AuthoriseAsAgentWithClient,
                                        val sessionTimeoutView: SessionTimeoutView,
                                        val unauthorisedAgentView: UnauthorisedAgentView,
                                        val userInsolventErrorView: UserInsolventErrorView,
                                        val notSignedUpView: NotSignedUpView,
                                        val appConfig: AppConfig,
                                        val executionContext: ExecutionContext,
                                        val messagesApi: MessagesApi)
