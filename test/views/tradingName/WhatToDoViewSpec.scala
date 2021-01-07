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

package views.tradingName

import forms.WhatToDoForm.{change, changeRemove, remove, whatToDoForm}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.tradingName.WhatToDoView

class WhatToDoViewSpec extends ViewBaseSpec {

  val injectedView: WhatToDoView = inject[WhatToDoView]

  object Selectors {
    val pageHeading = "h1"
    val backLink = ".govuk-back-link"
    val form = "form"
    val continueButton = ".govuk-button"
    val errorSummaryTitle = "#error-summary-title"
    val errorSummaryLink = ".govuk-error-summary__list > li > a"
    val radioChangeLabel = "label[for=change]"
    val radioRemoveLabel = "label[for=remove]"
    val radioChange = "#change"
    val radioRemove = "#remove"
    val errorRadioText = "#change_remove-error"
  }

  "Rendering the What To Do page" when {

    "the user is a principal entity" when {

      "the form has no errors" should {

        lazy val view: Html = injectedView(whatToDoForm)(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "What do you want to do with the current trading name? - Business tax account - GOV.UK"
        }

        "have a back link" which {

          "has the correct text" in {
            elementText(Selectors.backLink) shouldBe "Back"
          }

          "has the correct back link" in {
            element(Selectors.backLink).attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }
        }

        "have the correct page heading" in {
          elementText(Selectors.pageHeading) shouldBe "What do you want to do with the current trading name?"
        }

        "have the radio button 'Change'" which {

          "has the correct post value" in {
            element(Selectors.radioChange).attr("value") shouldBe change
          }

          "has the correct text" in {
            elementText(Selectors.radioChangeLabel) shouldBe "I want to change it to something else"
          }
        }

        "have the radio button 'Remove'" which {

          "has the correct post value" in {
            element(Selectors.radioRemove).attr("value") shouldBe remove
          }

          "has the correct text" in {
            elementText(Selectors.radioRemoveLabel) shouldBe "I want to remove it"
          }
        }

        "have the correct form action" in {
          element(Selectors.form).attr("action") shouldBe controllers.tradingName.routes.WhatToDoController.submit().url
        }

        "have the continue button" in {
          elementText(Selectors.continueButton) shouldBe "Continue"
        }
      }

      "the form has an error" should {

        lazy val view: Html = injectedView(whatToDoForm.bind(Map(changeRemove -> "")))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "Error: What do you want to do with the current trading name? - Business tax account - GOV.UK"
        }

        "display the error summary" in {
          elementText(Selectors.errorSummaryTitle) shouldBe "There is a problem"
        }

        "display the error summary link" which {

          "has the correct text" in {
            elementText(Selectors.errorSummaryLink) shouldBe "Select an answer"
          }

          "has the correct href" in {
            element(Selectors.errorSummaryLink).attr("href") shouldBe "#change"
          }
        }

        "display the radio button error text" in {
          elementText(Selectors.errorRadioText) shouldBe "Error: Select an answer"
        }
      }
    }

    "the user is an agent" should {

      lazy val view: Html = injectedView(whatToDoForm)(agent, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe "What do you want to do with the current trading name? - Your client’s VAT details - GOV.UK"
      }
    }
  }
}
