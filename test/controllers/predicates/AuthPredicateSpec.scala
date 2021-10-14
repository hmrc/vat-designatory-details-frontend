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

package controllers.predicates

import assets.CustomerInfoConstants.{customerInfoInsolvent, fullCustomerInfoModel}
import common.SessionKeys
import mocks.MockAuth
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import utils.MaterializerSupport
import play.api.test.Helpers._

import scala.concurrent.Future

class AuthPredicateSpec extends MockAuth with MaterializerSupport {

  val authPredicate: Action[AnyContent] = mockAuthPredicate.async {
    Future.successful(Ok("test"))
  }

  "The AuthPredicateSpec" when {

    "the user is an Agent" when {

      "the Agent has an active HMRC-AS-AGENT enrolment" should {

        lazy val result = {
          mockAgentAuthorised()
          authPredicate(agent)
        }

        "return OK (200)" in {
          status(result) shouldBe Status.OK
        }
      }
      "the Agent does NOT have an Active HMRC-AS-AGENT enrolment" should {

        lazy val result = {
          mockAgentWithoutEnrolment()
          authPredicate(agent)
        }

        "return Forbidden (403)" in {
          status(result) shouldBe Status.FORBIDDEN
        }

        "render the Unauthorised Agent page" in {
          messages(Jsoup.parse(contentAsString(result)).title) shouldBe
            "You can not use this service yet - Your client’s VAT details - GOV.UK"
        }
      }
    }

    "the agent does not have an affinity group" should {

      "return ISE (500)" in {
        mockUserWithoutAffinity()
        status(authPredicate(request)) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "a no active session result is returned from Auth" should {

      lazy val result = authPredicate(agent)

      "return Unauthorised (401)" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "render the Unauthorised page" in {
        messages(Jsoup.parse(contentAsString(result)).title) shouldBe "Your session has timed out - Your client’s VAT details - GOV.UK"
      }
    }

    "an authorisation exception is returned from Auth" should {

      lazy val result = authPredicate(agent)

      "return Internal Server Error (500)" in {
        mockAuthorisationException()
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "render the Unauthorised page" in {
        messages(Jsoup.parse(contentAsString(result)).title) shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
      }
    }
  }

  "the user is an Individual (Principle Entity)" when {

    "they have an active HMRC-MTD-VAT enrolment" when {

      "they have a value in session for their insolvency status" when {

        "the value is 'true' (insolvent user not continuing to trade)" should {

          "return Forbidden (403)" in {
            status(authPredicate(insolventRequest)) shouldBe Status.FORBIDDEN
          }
        }

        "the value is 'false' (user permitted to trade)" should {

          "return OK (200)" in {
            status(authPredicate(request)) shouldBe Status.OK
          }
        }
      }

      "they do not have a value in session for their insolvency status" when {

        "they are insolvent and not continuing to trade" should {

          lazy val result = {
            mockGetCustomerInfo("999999999")(Right(customerInfoInsolvent))
            authPredicate(FakeRequest())
          }

          "return Forbidden (403)" in {
            status(result) shouldBe Status.FORBIDDEN
          }

          "add the insolvent flag to the session" in {
            session(result).get(SessionKeys.insolventWithoutAccessKey) shouldBe Some("true")
          }
        }

        "they are permitted to trade" should {

          lazy val result = {
            mockGetCustomerInfo("999999999")(Right(fullCustomerInfoModel))
            authPredicate(FakeRequest())
          }

          "return OK (200)" in {
            status(result) shouldBe Status.OK
          }

          "add the insolvent flag to the session" in {
            session(result).get(SessionKeys.insolventWithoutAccessKey) shouldBe Some("false")
          }
        }

        "there is an error returned from the customer information API" should {

          lazy val result = {
            mockGetCustomerInfo("999999999")(Left(ErrorModel(INTERNAL_SERVER_ERROR, "")))
            authPredicate(FakeRequest())
          }

          "return Internal Server Error (500)" in {
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          }
        }
      }
    }

    "they do NOT have an active HMRC-MTD-VAT enrolment" should {

      lazy val result = authPredicate(user)

      "return Forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "render the Not Signed Up page" in {
        messages(Jsoup.parse(contentAsString(result)).title) shouldBe "You can not use this service yet - Manage your VAT account - GOV.UK"
      }
    }
  }
}
