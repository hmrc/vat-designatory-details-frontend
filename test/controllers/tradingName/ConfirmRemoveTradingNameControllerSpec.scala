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

package controllers.tradingName

import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys.validationTradingNameKey
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import controllers.ControllerBaseSpec
import forms.YesNoForm
import forms.YesNoForm.{yes, yesNo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.tradingName.ConfirmRemoveTradingNameView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveTradingNameControllerSpec extends ControllerBaseSpec {

  def setup(result: GetCustomerInfoResponse): Any = {
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))
  }

  def target(result: GetCustomerInfoResponse = Right(fullCustomerInfoModel)): ConfirmRemoveTradingNameController = {
    setup(result)
    new ConfirmRemoveTradingNameController(inject[ConfirmRemoveTradingNameView])
  }

  "Calling the show action" when {

    insolvencyCheck(target().show)

    "there is a validation trading name in session that is not empty ('remove' journey)" should {

      lazy val result = target().show(request.withSession(validationTradingNameKey -> "ABC Trading"))

      "return 200" in {
        status(result) shouldBe OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there is a validation trading name in session that is empty ('add' journey)" should {

      lazy val result = target().show(request.withSession(validationTradingNameKey -> ""))

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the capture trading name page" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show().url)
      }
    }

    "there is not a validation trading name in session" should {

      lazy val result = target().show(request)

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the capture trading name page" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show().url)
      }
    }
  }

  "Calling the submit action" when {

    insolvencyCheck(target().submit)

    "there is a validation trading name in session" when {

      "the form is bound as a Yes" should {

        lazy val result = target().submit(request
          .withSession(validationTradingNameKey -> "ABC Trading")
          .withFormUrlEncodedBody(yesNo -> yes))

        "return 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the update trading name method" in {
          redirectLocation(result) shouldBe Some(controllers.businessTradingName.routes.CheckYourAnswersController.updateTradingName().url)
        }
      }

      "the form is bound as a No" should {

        lazy val result = target().submit(request
          .withSession(validationTradingNameKey -> "ABC Trading")
          .withFormUrlEncodedBody(yesNo -> YesNoForm.no))

        "return 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the manage VAT page" in {
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }
    }

    "the form is bound with errors" should {

      lazy val result = target().submit(request
        .withSession(validationTradingNameKey -> "ABC Trading")
        .withFormUrlEncodedBody(yesNo -> ""))

      "return 400" in {
        status(result) shouldBe BAD_REQUEST
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there is not a validation trading name in session" should {

      lazy val result = target().submit(request)

      "return 500" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
