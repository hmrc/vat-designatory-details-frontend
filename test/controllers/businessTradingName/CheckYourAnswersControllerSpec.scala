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

package controllers.businessTradingName

import assets.BaseTestConstants.vrn
import audit.AuditingService
import common.SessionKeys
import common.SessionKeys.{businessNameAccessPermittedKey, validationTradingNameKey}
import controllers.ControllerBaseSpec
import forms.YesNoForm
import forms.YesNoForm.{yes, yesNo}
import models.customerInformation.{UpdateBusinessName, UpdateOrganisationDetailsSuccess, UpdateTradingName}
import models.errors.ErrorModel
import play.api.test.Helpers._
import views.html.businessTradingName.CheckYourAnswersView
import views.html.tradingName.ConfirmRemoveTradingNameView
import scala.concurrent.Future


class CheckYourAnswersControllerSpec extends ControllerBaseSpec {

  implicit val auditingService: AuditingService = inject[AuditingService]

  val controller = new CheckYourAnswersController(
    mockErrorHandler,
    inject[CheckYourAnswersView],
    inject[ConfirmRemoveTradingNameView],
    mockVatSubscriptionService
  )

  "Calling the show trading name action in CheckYourAnswersController" when {

    insolvencyCheck(controller.showTradingName)

    "there is a trading name in session" should {

      "show the Check your answer page" in {
        mockIndividualAuthorised()
        val result = controller.showTradingName()(getRequestWithNewTradingName)

        status(result) shouldBe OK
      }
    }

    "there isn't a trading name in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.showTradingName()(getRequest)
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to enter a new trading name" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show.url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.showTradingName()(getRequestWithNewTradingName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "Calling updateTradingName() in CheckYourAnswersController" when {

    "the user has an existing trading name" when {

      insolvencyCheck(controller.updateTradingName())

      "there is a non-empty trading name in session" when {

        "the trading name has been updated successfully" should {

          lazy val result = {
            mockUpdateTradingName(vrn, UpdateTradingName(Some(testTradingName), None))(
              Future(Right(UpdateOrganisationDetailsSuccess("someFormBundle"))))
            controller.updateTradingName()(postRequestWithNewTradingName)
          }

          "return 303" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to the trading name changed success page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.tradingName.url)
          }

          "add tradingNameChangeSuccessful to session" in {
            session(result).get(SessionKeys.tradingNameChangeSuccessful) shouldBe Some("true")
          }

          "add inFlightOrgDetailsKey to session" in {
            session(result).get(SessionKeys.inFlightOrgDetailsKey) shouldBe Some("true")
          }

          "add prepopulationTradingNameKey to session" in {
            session(result).get(SessionKeys.prepopulationTradingNameKey) shouldBe Some("Test Trading Name")
          }
        }

        "VatSubscriptionService returns a conflict" should {

          lazy val result = {
            mockUpdateTradingName(vrn, UpdateTradingName(Some(testTradingName), None))(Future(Left(ErrorModel(CONFLICT, "bad things"))))
            controller.updateTradingName()(postRequestWithNewTradingName)
          }

          "return 303" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to manage-vat" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
          }

          "add inFlightOrgDetailsKey to session" in {
            session(result).get(SessionKeys.inFlightOrgDetailsKey) shouldBe Some("true")
          }
        }

        "VatSubscriptionService returns an error" should {

          lazy val result = {
            mockUpdateTradingName(vrn, UpdateTradingName(Some(testTradingName), None))(Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "bad things, again"))))
            controller.updateTradingName()(postRequestWithNewTradingName)
          }

          "return 500" in {
            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "there isn't a trading name in session" should {

        lazy val result = {
          controller.updateTradingName()(postRequest)
        }

        "return 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect the user to the capture trading name page" in {
          redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show.url)
        }
      }
    }

    "the user doesn't have an existing trading name" when {

      "there is a non-empty trading name in session" when {

        "the trading name has been updated successfully" should {

          lazy val result = {
            mockUpdateTradingName(vrn, UpdateTradingName(Some(testTradingName), None))(
              Future(Right(UpdateOrganisationDetailsSuccess("someFormBundle"))))
            controller.updateTradingName()(postRequestWithoutExistingTradingName)
          }

          "return 303" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to the trading name changed success page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.tradingName.url)
          }

          "add tradingNameChangeSuccessful to session" in {
            session(result).get(SessionKeys.tradingNameChangeSuccessful) shouldBe Some("true")
          }

          "add inFlightOrgDetailsKey to session" in {
            session(result).get(SessionKeys.inFlightOrgDetailsKey) shouldBe Some("true")
          }

          "add prepopulationTradingNameKey to session" in {
            session(result).get(SessionKeys.prepopulationTradingNameKey) shouldBe Some("Test Trading Name")
          }
        }
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateTradingName()(postRequestWithNewTradingName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "Calling the showConfirmTradingNameRemoval action" when {

    insolvencyCheck(controller.showConfirmTradingNameRemoval)

    "there is a trading name in session" should {

      "show the Check your answer page" in {
        mockIndividualAuthorised()
        val result = controller.showConfirmTradingNameRemoval()(getRequestWithOnlyExistingTradingName)

        status(result) shouldBe OK
      }
    }

    "there is no trading name in session" should {

      mockIndividualAuthorised()
      val result = controller.showConfirmTradingNameRemoval()(getRequest)

      "have status 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the CaptureTradingNameController" in {
        redirectLocation(result) shouldBe Some(controllers.tradingName.routes.CaptureTradingNameController.show.url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.showConfirmTradingNameRemoval()(getRequestWithNewTradingName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "Calling the removeTradingName action" when {

    insolvencyCheck(controller.removeTradingName)

    "there is a validation trading name in session" when {

      "the form is bound as a Yes" should {

        lazy val result = {
          mockUpdateTradingName(vrn, UpdateTradingName(None, None))(
            Future(Right(UpdateOrganisationDetailsSuccess("someFormBundle"))))
          controller.removeTradingName()(postRequest
            .withSession(validationTradingNameKey -> "ABC Trading")
            .withFormUrlEncodedBody(yesNo -> yes))
        }

        "return 303" in {
          mockIndividualAuthorised()
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the Change success controller action" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.tradingName.url)
        }

        "add tradingNameChangeSuccessful to session" in {
          session(result).get(SessionKeys.tradingNameChangeSuccessful) shouldBe Some("true")
        }

        "add inFlightOrgDetailsKey to session" in {
          session(result).get(SessionKeys.inFlightOrgDetailsKey) shouldBe Some("true")
        }

        "add prepopulationTradingNameKey to session" in {
          session(result).get(SessionKeys.prepopulationTradingNameKey) shouldBe Some("")
        }
      }

      "the form is bound as a No" should {

        lazy val result = controller.removeTradingName()(postRequest
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

      lazy val result = controller.removeTradingName()(postRequest
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

    "there is no validation trading name in session" should {

      lazy val result = controller.removeTradingName()(postRequest)

      "return 500" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "Calling the show business name action in CheckYourAnswersController" when {

    insolvencyCheck(controller.showBusinessName)

    "there is a business name in session" should {

      "show the Check your answer page" in {
        mockIndividualAuthorised()
        val result = controller.showBusinessName()(getRequestWithBusinessName.withSession(
          businessNameAccessPermittedKey -> "true"))

        status(result) shouldBe OK
      }
    }

    "there isn't a business name in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.showBusinessName()(getRequest.withSession(
          businessNameAccessPermittedKey -> "true"))
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to enter a new business name" in {
        redirectLocation(result) shouldBe Some(controllers.businessName.routes.CaptureBusinessNameController.show.url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.showBusinessName()(getRequestWithBusinessName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "Calling updateBusinessName() in CheckYourAnswersController" when {

    insolvencyCheck(controller.updateBusinessName())

    "there is a business name in session" when {

      "the business name has been updated successfully" should {

        lazy val result = {
          mockUpdateBusinessName(vrn, UpdateBusinessName(testBusinessName, None))(
            Future(Right(UpdateOrganisationDetailsSuccess("someFormBundle")))
          )
          controller.updateBusinessName()(postRequestWithBusinessName.withSession(
            businessNameAccessPermittedKey -> "true",

          ))
        }

        "return 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the business name changed success page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.businessName.url)
        }

        "add businessChangeSuccessful" in {
          session(result).get(SessionKeys.businessNameChangeSuccessful) shouldBe Some("true")
        }

        "add inFlightOrgDetailsKey to session" in {
          session(result).get(SessionKeys.inFlightOrgDetailsKey) shouldBe Some("true")
        }
      }
    }

    "VatSubscriptionService returns a conflict" should {

      lazy val result = {
        mockUpdateBusinessName(vrn, UpdateBusinessName(testBusinessName, None))(
          Future(Left(ErrorModel(CONFLICT, "bad things")))
        )
        controller.updateBusinessName()(postRequestWithBusinessName.withSession(
          businessNameAccessPermittedKey -> "true"))
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to manage-vat" in {
        redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
      }

      "add tinFlightOrgDetailsKey to session" in {
        session(result).get(SessionKeys.inFlightOrgDetailsKey) shouldBe Some("true")
      }
    }

    "VatSubscriptionService returns an error" should {

      lazy val result = {
        mockUpdateBusinessName(vrn, UpdateBusinessName(testBusinessName, None))(
          Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "bad things, again")))
        )
        controller.updateBusinessName()(postRequestWithBusinessName.withSession(
          businessNameAccessPermittedKey -> "true"))
      }

      "return 500" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there isn't a business name in session" should {

      lazy val result = {
        controller.updateBusinessName()(postRequest.withSession(
          businessNameAccessPermittedKey -> "true"))
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the capture business name page" in {
        redirectLocation(result) shouldBe Some(controllers.businessName.routes.CaptureBusinessNameController.show.url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateBusinessName()(postRequestWithBusinessName)

        status(result) shouldBe FORBIDDEN
      }
    }
  }
}
