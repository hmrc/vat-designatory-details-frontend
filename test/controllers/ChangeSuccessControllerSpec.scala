/*
 * Copyright 2022 HM Revenue & Customs
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

import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys._
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import views.html.businessName.BusinessNameChangeSuccessView
import views.html.tradingName.TradingNameChangeSuccessView

import scala.concurrent.Future

class ChangeSuccessControllerSpec extends ControllerBaseSpec {

  val controller: ChangeSuccessController = new ChangeSuccessController(
    inject[TradingNameChangeSuccessView],
    inject[BusinessNameChangeSuccessView],
    mockErrorHandler
  )

  "Calling the tradingName action" when {

    insolvencyCheck(controller.tradingName)

    "all three expected session keys are populated" when {

      "the user is a principal entity" should {
        lazy val result: Future[Result] = {
          mockGetCustomerInfo("999999999")(Right(fullCustomerInfoModel))
          controller.tradingName()(request.withSession(
            tradingNameChangeSuccessful -> "true", prepopulationTradingNameKey -> testTradingName, validationTradingNameKey -> "Test"
          ))
        }

        "return 200" in {
          status(result) shouldBe Status.OK
        }
      }

      "the user is an agent" should {
        lazy val result: Future[Result] = {
          mockGetCustomerInfo("111111111")(Right(fullCustomerInfoModel))
          controller.tradingName()(request.withSession(
            tradingNameChangeSuccessful -> "true", prepopulationTradingNameKey -> testTradingName, validationTradingNameKey -> "Test",
            mtdVatvcClientVrn -> "111111111"
          ))
        }

        "return 200" in {
          mockAgentAuthorised()
          status(result) shouldBe Status.OK
        }
      }

      "the call to CustomerInfo returns an error" should {
        lazy val result: Future[Result] = {
          mockGetCustomerInfo("999999999")(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "")))
          controller.tradingName()(request.withSession(
            tradingNameChangeSuccessful -> "true", prepopulationTradingNameKey -> testTradingName, validationTradingNameKey -> "Test"
          ))
        }

        "return 200" in {
          status(result) shouldBe Status.OK
        }
      }
    }

    "one or more of the expected session keys is missing" should {

      lazy val result: Future[Result] = controller.tradingName()(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture trading name controller" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show.url)
      }
    }
  }

  "the renderTradingNameView function" when {

    "the user is adding a trading name" should {

      lazy val result = {
        mockGetCustomerInfo("999999999")(Right(fullCustomerInfoModel))
        controller.renderTradingNameView(isRemoval = false, isAddition = true)(user)
      }

      lazy val document = Jsoup.parse(contentAsString(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "has the correct message key for the title" in {
        document.select("h1").text() shouldBe "You have asked to add a trading name"
      }

    }

    "the user is removing a trading name" should {

      lazy val result = {
        mockGetCustomerInfo("999999999")(Right(fullCustomerInfoModel))
        controller.renderTradingNameView(isRemoval = true, isAddition = false)(user)
      }

      lazy val document = Jsoup.parse(contentAsString(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "has the correct message key for the title" in {
        document.select("h1").text() shouldBe "You have asked to remove the trading name"
      }

    }

    "the user is updating a trading name" should {

      lazy val result = {
        mockGetCustomerInfo("999999999")(Right(fullCustomerInfoModel))
        controller.renderTradingNameView(isRemoval = false, isAddition = false)(user)
      }

      lazy val document = Jsoup.parse(contentAsString(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "has the correct message key for the title" in {
        document.select("h1").text() shouldBe "You have asked to change the trading name"
      }

    }

    "the isRemoval and isAddition parameters are both true" should {

      lazy val result = {
        mockGetCustomerInfo("999999999")(Right(fullCustomerInfoModel))
        controller.renderTradingNameView(isRemoval = true, isAddition = true)(user)
      }

      "return 500" in {
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }

  "Calling the businessName action" when {

    "the business name feature switch is on" when {

      insolvencyCheck(controller.businessName)

      "the businessNameChangeSuccessful session key is 'true'" when {

        "the user is a principal entity" should {
          lazy val document = Jsoup.parse(contentAsString(result))

          lazy val result: Future[Result] = {
            controller.businessName()(request.withSession(businessNameChangeSuccessful -> "true"))
          }

          "return 200" in {
            status(result) shouldBe Status.OK
          }

          "render the correct heading" in {
            document.select("h1").text() shouldBe "You have asked to change the business name"
          }
        }

        "the user is an agent" should {
          lazy val result: Future[Result] = {
            mockGetCustomerInfo("111111111")(Right(fullCustomerInfoModel))
            controller.businessName()(request.withSession(
              businessNameChangeSuccessful -> "true", mtdVatvcClientVrn -> "111111111"
            ))
          }

          "return 200" in {
            mockAgentAuthorised()
            status(result) shouldBe Status.OK
          }
        }
      }

      "the businessNameChangeSuccessful session key is not populated" should {

        lazy val result: Future[Result] = controller.businessName()(request)

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the capture business name controller" in {
          redirectLocation(result) shouldBe Some(controllers.businessName.routes.CaptureBusinessNameController.show.url)
        }
      }
    }

    "the business name feature is off" should {

      "return not found (404)" in {

        lazy val result: Future[Result] = {
          mockConfig.features.businessNameR19_R20Enabled(false)
          mockGetCustomerInfo("111111111")(Right(fullCustomerInfoModel))
          controller.businessName()(request)
        }

        status(result) shouldBe NOT_FOUND
      }
    }
  }
}
