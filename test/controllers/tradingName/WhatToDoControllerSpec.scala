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

package controllers.tradingName

import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys
import common.SessionKeys.validationTradingNameKey
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import controllers.ControllerBaseSpec
import forms.WhatToDoForm.{change, changeRemove, remove}
import models.errors.ErrorModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.tradingName.WhatToDoView

import scala.concurrent.{ExecutionContext, Future}

class WhatToDoControllerSpec extends ControllerBaseSpec {

  def setup(result: GetCustomerInfoResponse): Any =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  def target(result: GetCustomerInfoResponse = Right(fullCustomerInfoModel)): WhatToDoController = {
    setup(result)
    new WhatToDoController(inject[WhatToDoView], mockVatSubscriptionService)
  }

  "Calling the show action" when {

    insolvencyCheck(target().show)

    "there is a validation trading name in session that is not empty ('change' or 'remove' journey)" should {

      lazy val result = target().show()(getRequest.withSession(validationTradingNameKey -> "ABC Trading"))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "not call the VatSubscription service" in {
        verify(mockVatSubscriptionService, never())
          .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
      }

      "add the validation trading name key to session" in {
        session(result).get(SessionKeys.validationTradingNameKey) shouldBe Some("ABC Trading")
      }
    }

    "there is a validation trading name in session that is empty ('add' journey)" should {

      lazy val result = target().show()(getRequest.withSession(validationTradingNameKey -> ""))

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the CaptureTradingNameController show action" in {
        redirectLocation(result) shouldBe Some(routes.CaptureTradingNameController.show.url)
      }

      "not call the VatSubscription service" in {
        verify(mockVatSubscriptionService, never())
          .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
      }

      "add the validation trading name key to session" in {
        session(result).get(SessionKeys.validationTradingNameKey) shouldBe Some("")
      }
    }

    "there is not a validation trading name in session" when {

      "the customerInfo call succeeds" should {

        lazy val result = target().show()(getRequest)

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "add the validation trading name key to session" in {
          session(result).get(SessionKeys.validationTradingNameKey) shouldBe Some("PepsiMac")
        }
      }

      "the customerInfo call fails" should {

        lazy val result = target(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "error"))).show()(postRequest)

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
  }

  "Calling the submit action" when {

    insolvencyCheck(target().submit)

    "there is a validation trading name in session" when {

      "the form is bound as a 'Change'" should {

        lazy val result = target().submit()(postRequest
          .withSession(validationTradingNameKey -> "ABC Trading")
          .withFormUrlEncodedBody(changeRemove -> change))

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the CaptureTradingNameController show action" in {
          redirectLocation(result) shouldBe Some(routes.CaptureTradingNameController.show.url)
        }
      }

      "the form is bound as a 'Remove'" should {

        lazy val result = target().submit()(postRequest.
          withSession(validationTradingNameKey -> "ABC Trading")
          .withFormUrlEncodedBody(changeRemove -> remove))

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the CheckYourAnswersController showConfirmTradingNameRemoval action" in {
          redirectLocation(result) shouldBe Some(controllers.businessTradingName.routes.CheckYourAnswersController.showConfirmTradingNameRemoval.url)
        }
      }

      "the form is bound with errors" should {

        lazy val result = target().submit()(postRequest.
          withSession(validationTradingNameKey -> "ABC Trading")
          .withFormUrlEncodedBody(changeRemove -> ""))

        "return 400" in {
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "there is not a validation trading name in session" should {

      lazy val result = target().submit()(postRequest)

      "return 500" in {
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
