/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.predicates.inflight

import assets.CustomerInfoConstants._
import common.SessionKeys.inFlightTradingNameChangeKey
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import mocks.MockAuth
import models.User
import models.customerInformation.PendingChanges
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class InFlightPredicateSpec extends MockAuth {

  def setup(result: GetCustomerInfoResponse = Right(customerInfoPendingTradingNameModel)): Unit =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  val inflightTradingNamePredicate = new InFlightPredicate(
    mockInFlightPredicateComponents,
    "/redirect-location"
  )

  def userWithSession(inflightTradingNameValue: String): User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type]("999943620")(request.withSession(inFlightTradingNameChangeKey -> inflightTradingNameValue))

  val userWithoutSession: User[AnyContentAsEmpty.type] = User("999999999")(FakeRequest())

  "The InFlightPredicate" when {

    "there is an inflight indicator in session" when {

      "the inflight indicator is set to a change value" should {

        lazy val result = await(inflightTradingNamePredicate.refine(userWithSession("tradingName"))).left.get
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "You already have a change pending - Business tax account - GOV.UK"
        }

        "not call the VatSubscriptionService" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }

      "the inflight indicator is set to 'false'" should {

        lazy val result = await(inflightTradingNamePredicate.refine(userWithSession("false")))

        "allow the request to pass through the predicate" in {
          result shouldBe Right(userWithSession("false"))
        }

        "not call the VatSubscriptionService" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }
    }

    "there is no inflight indicator in session" when {

      "the user has an inflight trading name" should {

        lazy val result = {
          setup()
          await(inflightTradingNamePredicate.refine(userWithoutSession)).left.get
        }
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "add the inflight indicator 'tradingName' to session" in {
          session(result).get(inFlightTradingNameChangeKey) shouldBe Some("tradingName")
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "You already have a change pending - Business tax account - GOV.UK"
        }
      }

      "the user has no inflight information" should {

        lazy val result = {
          setup(Right(minCustomerInfoModel))
          await(inflightTradingNamePredicate.refine(userWithoutSession)).left.get
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the predicate's redirect URL" in {
          redirectLocation(result) shouldBe Some("/redirect-location")
        }

        "add the inflight indicator 'false' to session" in {
          session(result).get(inFlightTradingNameChangeKey) shouldBe Some("false")
        }
      }

      "the user has a change pending which isn't trading name" should {

        lazy val result = {
          setup(Right(fullCustomerInfoModelSameTradingName))
          await(inflightTradingNamePredicate.refine(userWithoutSession))
        }

        "pass through the predicate" in {
          result shouldBe Right(userWithoutSession)
        }
      }

      "the service call fails" should {

        lazy val result = {
          setup(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "error")))
          await(inflightTradingNamePredicate.refine(userWithoutSession)).left.get
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
