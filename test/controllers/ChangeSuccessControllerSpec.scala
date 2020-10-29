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

package controllers

import assets.BaseTestConstants._
import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys._
import models.User
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import views.html.templates.ChangeSuccessView

import scala.concurrent.Future

class ChangeSuccessControllerSpec extends ControllerBaseSpec {

  val controller: ChangeSuccessController = new ChangeSuccessController(
    mockVatSubscriptionService,
    inject[ChangeSuccessView]
  )

  "Calling the tradingName action" when {

    "both expected session keys are populated" when {

      "the user is a principal entity" should {
        lazy val result: Future[Result] = {
          controller.tradingName(request.withSession(
            tradingNameChangeSuccessful -> "true", prepopulationTradingNameKey -> testTradingName, validationTradingNameKey -> "Test"
          ))
        }

        "return 200" in {
          mockIndividualAuthorised()
          status(result) shouldBe Status.OK
        }
      }

      "the user is an agent" should {
        lazy val result: Future[Result] = {
          controller.tradingName(request.withSession(
            tradingNameChangeSuccessful -> "true", prepopulationTradingNameKey -> testTradingName,
            clientVrn -> "1111111111"
          ))
        }

        "return 200" in {
          mockAgentAuthorised()
          mockGetCustomerInfo("1111111111")(Future.successful(Right(fullCustomerInfoModel)))
          status(result) shouldBe Status.OK
        }
      }
    }

    "one or more of the expected session keys is missing" should {

      lazy val result: Future[Result] = controller.tradingName(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture trading name controller" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show().url)
      }
    }
  }

  "The .getClientEntityName function" when {

    "there is an entity name in session" should {

      lazy val result = controller.getClientEntityName(User(vrn, arn = Some(arn))(request.withSession(
        mtdVatAgentClientName -> "Jorip Biscuit Co"
      )))

      "return the entity name" in {
        await(result) shouldBe Some("Jorip Biscuit Co")
      }

      "not call the VatSubscription service" in {
        verify(mockVatSubscriptionService, times(0)).getCustomerInfo(any())(any(), any())
      }
    }

    "there is not an entity name in session" when {

      "the call to VatSubscription is successful" should {

        "return the entity name" in {
          val result = {
            mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel)))
            controller.getClientEntityName(User(vrn, arn = Some(arn))(request))
          }

          await(result) shouldBe Some("PepsiMac")
        }
      }

      "the call to VatSubscription is unsuccessful" should {

        "return None" in {
          val result = {
            mockGetCustomerInfo(vrn)(Future.successful(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Error"))))
            controller.getClientEntityName(User(vrn, arn = Some(arn))(request))
          }

          await(result) shouldBe None
        }
      }
    }
  }

  "the renderView function" when {

    "the user is adding a trading name" should {

      lazy val result = controller.renderView(false, true)(user)

      lazy val document = Jsoup.parse(bodyOf(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "has the correct message key for the title" in {
        document.select("h1").text() shouldBe "tradingNameChangeSuccess.title.add"
      }

    }

    "the user is removing a trading name" should {

      lazy val result = controller.renderView(true, false)(user)

      lazy val document = Jsoup.parse(bodyOf(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "has the correct message key for the title" in {
        document.select("h1").text() shouldBe "tradingNameChangeSuccess.title.remove"
      }

    }

    "the user is updating a trading name" should {

      lazy val result = controller.renderView(false, false)(user)

      lazy val document = Jsoup.parse(bodyOf(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "has the correct message key for the title" in {
        document.select("h1").text() shouldBe "tradingNameChangeSuccess.title.change"
      }

    }

    "the isRemoval and isAddition parameters ae both true" should {

      lazy val result = controller.renderView(true, true)(user)

      "return 500" in {
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

    }
  }
}
