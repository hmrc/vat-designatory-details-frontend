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

package utils

import assets.BaseTestConstants._
import common.SessionKeys
import common.SessionKeys._
import config.ErrorHandler
import mocks.MockAppConfig
import models.User
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.test.Helpers.CONTENT_TYPE
import views.html.errors.StandardErrorView

import scala.concurrent.ExecutionContext

trait TestUtil extends AnyWordSpecLike with Matchers with OptionValues
  with GuiceOneAppPerSuite with MaterializerSupport with BeforeAndAfterEach with Injecting {

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  implicit lazy val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  implicit lazy val messagesApi: MessagesApi = inject[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang("en-GB"), messagesApi)
  implicit lazy val mockConfig: MockAppConfig = new MockAppConfig(app.configuration)

  lazy val mockErrorHandler: ErrorHandler = new ErrorHandler(messagesApi, inject[StandardErrorView], mockConfig)

  val testTradingName = "Test Trading Name"
  val testBusinessName = "Test Business Name"

  val oldBusinessName = "Old Business Name"
  val oldTradingName = "Old trading name"

  implicit lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/path")
    .withSession(
      inFlightOrgDetailsKey -> "false",
      insolventWithoutAccessKey -> "false")

  implicit lazy val postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/path")
    .withHeaders(CONTENT_TYPE -> "application/x-www-form-urlencoded")
    .withSession(
    inFlightOrgDetailsKey -> "false",
    insolventWithoutAccessKey -> "false")

  lazy val getRequestWithNewTradingName: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(prepopulationTradingNameKey -> testTradingName)

  lazy val postRequestWithNewTradingName: FakeRequest[AnyContentAsEmpty.type] =
    postRequest.withSession(prepopulationTradingNameKey -> testTradingName)

  lazy val getRequestWithOnlyExistingTradingName: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(validationTradingNameKey -> oldTradingName)

  lazy val postRequestWithoutExistingTradingName: FakeRequest[AnyContentAsEmpty.type] =
    postRequest.withSession(prepopulationTradingNameKey -> testTradingName, validationTradingNameKey -> "")

  lazy val getRequestWithBusinessName: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(prepopulationBusinessNameKey -> testBusinessName, validationBusinessNameKey -> oldBusinessName)

  lazy val postRequestWithBusinessName: FakeRequest[AnyContentAsEmpty.type] =
    postRequest.withSession(prepopulationBusinessNameKey -> testBusinessName, validationBusinessNameKey -> oldBusinessName)

  lazy val getRequestWithoutExistingBusinessName: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(validationBusinessNameKey -> "")

  lazy val fakeRequestWithClientsVRN: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(mtdVatvcClientVrn -> vrn)

  lazy val insolventRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.insolventWithoutAccessKey -> "true")

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(getRequest)
  lazy val agent: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true, Some(arn))(fakeRequestWithClientsVRN)

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = mcc.executionContext
}
