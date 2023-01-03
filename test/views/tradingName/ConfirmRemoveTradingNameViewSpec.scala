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

package views.tradingName

import forms.YesNoForm
import models.YesNo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.ViewBaseSpec
import views.html.tradingName.ConfirmRemoveTradingNameView
import assets.ConfirmRemoveMessages

class ConfirmRemoveTradingNameViewSpec extends ViewBaseSpec {

  val injectedView: ConfirmRemoveTradingNameView = inject[ConfirmRemoveTradingNameView]

  object Selectors {
    val heading = ".govuk-fieldset__legend"
    val continueButton = ".govuk-button"
    val yesOption = "div.govuk-radios__item:nth-child(1)"
    val noOption = "div.govuk-radios__item:nth-child(2)"
    val error = ".govuk-error-message"
    val errorHeading = "#error-summary-title"
    val errorLink = ".govuk-list > li > a"
    val errorMessage = "#yes_no-error"
    val backLink = ".govuk-back-link"
  }

  "The confirm remove trading name view with no errors" when {

    "there are no form errors" should {

      val form: Form[YesNo] = YesNoForm.yesNoForm("confirmRemove.error")
      lazy val view = injectedView(form, testTradingName)(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe ConfirmRemoveMessages.title
      }

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe ConfirmRemoveMessages.heading(testTradingName)
      }

      "have the correct continue button text" in {
        elementText(Selectors.continueButton) shouldBe ConfirmRemoveMessages.confirm
      }

      "have the prevent double click attribute on the continue" in {
        element(Selectors.continueButton).hasAttr("data-prevent-double-click") shouldBe true
      }

      "display the Yes option" in {
        elementText(Selectors.yesOption) shouldBe ConfirmRemoveMessages.yes
      }

      "display the No option" in {
        elementText(Selectors.noOption) shouldBe ConfirmRemoveMessages.no
      }

      "have the correct back link" which {

        "should have the correct text" in {
          elementText(Selectors.backLink) shouldBe ConfirmRemoveMessages.back
        }

        "should have the correct href" in {
          element(Selectors.backLink).attr("href") shouldBe
            controllers.tradingName.routes.WhatToDoController.show.url
        }
      }

      "not display an error" in {
        document.select(Selectors.error).isEmpty shouldBe true
      }
    }

    "there are errors" should {
      val form: Form[YesNo] = YesNoForm.yesNoForm("confirmRemove.error").bind(Map(YesNoForm.yesNo -> ""))
      lazy val view = injectedView(form, testTradingName)(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe ConfirmRemoveMessages.errorTitle
      }

      "display an error summary" which {

        "has the correct title text" in {
          elementText(Selectors.errorHeading) shouldBe ConfirmRemoveMessages.errorHeading
        }

        "has an error link" which {

          "has the correct text" in {
            elementText(Selectors.errorLink) shouldBe ConfirmRemoveMessages.errorLinkText
          }
          "has a link to the error" in {
            element(Selectors.errorLink).attr("href") shouldBe "#yes_no-yes"
          }
        }
      }

      "display the correct error message" in {
        elementText(Selectors.errorMessage) shouldBe ConfirmRemoveMessages.errorText
      }
    }
  }
}
