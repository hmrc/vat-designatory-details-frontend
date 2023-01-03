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

package controllers.predicates.inflight

import assets.CustomerInfoConstants._
import common.SessionKeys.{businessNameAccessPermittedKey, inFlightOrgDetailsKey}
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import mocks.MockAuth
import models.User
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.BadRequest
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.{ExecutionContext, Future}

class InFlightPredicateSpec extends MockAuth {

  def setup(result: GetCustomerInfoResponse = Right(fullCustomerInfoModel)): Unit =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  val inflightTradingNamePredicate = new InFlightPredicate(
    mockInFlightPredicateComponents,
    "/redirect-location",
    businessNameJourney = false
  )

  val inflightBusinessNamePredicate = new InFlightPredicate(
    mockInFlightPredicateComponents,
    "/redirect-location",
    businessNameJourney = true
  )

  def inflightRequest(value: String): User[AnyContentAsEmpty.type] =
    User("999999999")(postRequest.withSession(inFlightOrgDetailsKey -> value))

  def inflightRequestWithAccess(value: String, access: String): User[AnyContentAsEmpty.type] =
    User("999999999")(postRequest.withSession(inFlightOrgDetailsKey -> value, businessNameAccessPermittedKey -> access))

  val userWithoutSession: User[AnyContentAsEmpty.type] = User("999999999")(FakeRequest())

  "The InFlightPredicate" when {

    "businessNameJourney is set to 'false'" when {

      "there is an inflight indicator in session" when {

        "the inflight indicator is set to 'true'" should {

          lazy val result = Future.successful(await(inflightTradingNamePredicate.refine(inflightRequest("true"))).swap.getOrElse(BadRequest))
          lazy val document = Jsoup.parse(contentAsString(result))

          "return 409" in {
            status(result) shouldBe Status.CONFLICT
          }

          "show the 'change pending' error page" in {
            messages(document.title) shouldBe "You cannot request another change now - Manage your VAT account - GOV.UK"
          }

          "not call the VatSubscriptionService" in {
            verify(mockVatSubscriptionService, never())
              .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
          }
        }

        "the inflight indicator is set to 'false'" should {

          lazy val result = await(inflightTradingNamePredicate.refine(inflightRequest("false")))

          "allow the request to pass through the predicate" in {
            result shouldBe Right(inflightRequest("false"))
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
            Future.successful(await(inflightTradingNamePredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
          }
          lazy val document = Jsoup.parse(contentAsString(result))

          "return 409" in {
            status(result) shouldBe Status.CONFLICT
          }

          "add the inflight indicator 'true' to session" in {
            session(result).get(inFlightOrgDetailsKey) shouldBe Some("true")
          }

          "show the 'change pending' error page" in {
            messages(document.title) shouldBe "You cannot request another change now - Manage your VAT account - GOV.UK"
          }
        }

        "the user has no inflight information" should {

          lazy val result = {
            setup(Right(minCustomerInfoModel))
            Future.successful(await(inflightTradingNamePredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to the predicate's redirect URL" in {
            redirectLocation(result) shouldBe Some("/redirect-location")
          }

          "add the inflight indicator 'false' to session" in {
            session(result).get(inFlightOrgDetailsKey) shouldBe Some("false")
          }
        }

        "the service call fails" should {

          lazy val result = {
            setup(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "error")))
            Future.successful(await(inflightTradingNamePredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
          }

          "return 500" in {
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          }
        }
      }
    }

    "businessNameJourney is set to 'true'" when {

      "there is an access permission of 'true' in session" when {

        "the inflight indicator is set to 'true'" should {

          lazy val result = Future.successful(await(inflightBusinessNamePredicate
            .refine(inflightRequestWithAccess("true", "true"))).swap.getOrElse(BadRequest))
          lazy val document = Jsoup.parse(contentAsString(result))

          "return 409" in {
            status(result) shouldBe Status.CONFLICT
          }

          "show the 'change pending' error page" in {
            messages(document.title) shouldBe "You cannot request another change now - Manage your VAT account - GOV.UK"
          }

          "not call the VatSubscriptionService" in {
            verify(mockVatSubscriptionService, never())
              .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
          }
        }

        "the inflight indicator is set to 'false'" should {

          lazy val result = await(inflightBusinessNamePredicate.refine(inflightRequestWithAccess("false", "true")))

          "allow the request to pass through the predicate" in {
            result shouldBe Right(inflightRequestWithAccess("false", "true"))
          }

          "not call the VatSubscriptionService" in {
            verify(mockVatSubscriptionService, never())
              .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
          }
        }
      }

      "there is an access permission of 'false' in session, regardless of inflight indicator" should {

        lazy val result = {
          setup(Right(minCustomerInfoModel))
          Future.successful(await(inflightBusinessNamePredicate
            .refine(inflightRequestWithAccess("false", "false"))).swap.getOrElse(BadRequest))
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to manage VAT overview page" in {
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there is no access permission in session" when {

        "the user has the correct customer info to allow them into the journey" should {

          lazy val result = {
            setup(Right(customerInfoNoPending))
            Future.successful(await(inflightBusinessNamePredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to the predicate's redirect URL" in {
            redirectLocation(result) shouldBe Some("/redirect-location")
          }

          "add the access permission 'true' to session" in {
            session(result).get(businessNameAccessPermittedKey) shouldBe Some("true")
          }

          "add the inflight indicator 'false' to session" in {
            session(result).get(inFlightOrgDetailsKey) shouldBe Some("false")
          }
        }

        "the user has an invalid party type" should {

          lazy val result = {
            setup(Right(customerInfoNoPending.copy(partyType = Some("F"))))
            Future.successful(await(inflightBusinessNamePredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to manage VAT overview page" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
          }

          "add the access permission 'false' to session" in {
            session(result).get(businessNameAccessPermittedKey) shouldBe Some("false")
          }

          "add the inflight indicator 'false' to session" in {
            session(result).get(inFlightOrgDetailsKey) shouldBe Some("false")
          }
        }

        "the user does not have a business name" should {

          lazy val result = {
            setup(Right(customerInfoNoPending.copy(organisationName = None)))
            Future.successful(await(inflightBusinessNamePredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to manage VAT overview page" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
          }

          "add the access permission 'false' to session" in {
            session(result).get(businessNameAccessPermittedKey) shouldBe Some("false")
          }

          "add the inflight indicator 'false' to session" in {
            session(result).get(inFlightOrgDetailsKey) shouldBe Some("false")
          }
        }

        "the user does not have nameIsReadOnly set to 'false'" should {

          lazy val result = {
            setup(Right(customerInfoNoPending.copy(nameIsReadOnly = Some(true))))
            Future.successful(await(inflightBusinessNamePredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to manage VAT overview page" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
          }

          "add the access permission 'false' to session" in {
            session(result).get(businessNameAccessPermittedKey) shouldBe Some("false")
          }

          "add the inflight indicator 'false' to session" in {
            session(result).get(inFlightOrgDetailsKey) shouldBe Some("false")
          }
        }
      }
    }
  }
}
