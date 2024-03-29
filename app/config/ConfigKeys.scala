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

package config

object ConfigKeys {
  val contactFrontendService: String = "contact-frontend.url"
  val contactFrontendIdentifier: String = "contact-frontend.serviceId"

  val govUkSetupAgentServices: String = "govuk.guidance.setupAgentServices.url"
  val govUkCommercialSoftware: String = "govuk.software.commercialSoftware.url"

  val governmentGatewayHost: String = "government-gateway.host"

  val signInBaseUrl: String = "signIn.url"

  val allowListEnabled: String = "allowList.enabled"
  val allowListedIps: String = "allowList.allowedIps"
  val allowListExcludedPaths: String = "allowList.excludedPaths"
  val allowListShutterPage: String = "allowList.shutter-page-url"
  val vatAgentClientLookupServiceUrl: String = "vat-agent-client-lookup-frontend.url"
  val vatAgentClientLookupServicePath: String = "vat-agent-client-lookup-frontend.path"
  val vatAgentClientLookupHubPath: String = "vat-agent-client-lookup-frontend.agentHub"
  val vatAgentClientLookupUnauthorisedForClient: String = "vat-agent-client-lookup-frontend.unauthorisedForClient"
  val manageVatSubscriptionServiceUrl: String = "manage-vat-subscription-frontend.url"
  val manageVatSubscriptionServicePath: String = "manage-vat-subscription-frontend.path"
  val vatSummaryFrontendHost: String = "vat-summary-frontend.host"
  val vatSummaryFrontendUrl: String = "vat-summary-frontend.url"

  val host: String = "host"

  val vatSubscription: String = "vat-subscription"

  val surveyUrl: String = "feedback-frontend.url"
  val surveyPath: String = "feedback-frontend.path"

  val timeoutPeriod: String = "timeout.period"
  val timeoutCountdown: String = "timeout.countdown"

  val contactHmrc: String = "contact-hmrc.url"

  val gtmContainer: String = "tracking-consent-frontend.gtm.container"

  val businessTaxAccountHost: String = "business-tax-account.host"
  val businessTaxAccountUrl: String = "business-tax-account.homeUrl"
}
