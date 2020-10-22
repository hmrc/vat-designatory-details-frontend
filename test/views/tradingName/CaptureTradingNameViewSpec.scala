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

package views.tradingName

import forms.TradingNameForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.tradingName.CaptureTradingNameView

class CaptureTradingNameViewSpec extends ViewBaseSpec {

  val injectedView: CaptureTradingNameView = inject[CaptureTradingNameView]

  object Selectors {
    val pageHeading = "#content h1"
    val backLink = ".govuk-back-link"
    val hintText = "#trading-name-hint"
    val form = "form"
    val tradingNameField = "#trading-name"
    val continueButton = ".govuk-button"
    val errorSummary = "#trading-name-error"
    val errorSummaryTitle = "#error-summary-title"
    val errorSummaryLink = ".govuk-error-summary__list > li > a"
    val tradingNameFormGroup = "#content > article > form > div:nth-child(1)"
    val removeTradingName = "#remove-trading-name"
  }

  "Rendering the capture trading name page" when {

    "the user is a principle entity" when {

      "the form has no errors" should {

        "the user already has a trading name in ETMP" should {
          lazy val view: Html = injectedView(tradingNameForm(testTradingName).fill(testTradingName),
            testTradingName)(user, messages, mockConfig)

          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct document title" in {
            document.title shouldBe "What is the trading name? - Business tax account - GOV.UK"
          }

          "have a back link" which {

            "should have the correct text" in {
              elementText(Selectors.backLink) shouldBe "Back"
            }

            "should have the correct back link" in {
              element(Selectors.backLink).attr("href") shouldBe "mockManageVatOverviewUrl"
            }
          }

          "have the correct page heading" in {
            elementText(Selectors.pageHeading) shouldBe "What is the trading name?"
          }

          "have the correct hint text" in {
            elementText(Selectors.hintText) shouldBe "Trading names cannot be longer than 160 characters"
          }

          "have the trading name form with the correct form action" in {
            element(Selectors.form).attr("action") shouldBe "/vat-through-software/account/designatory/new-trading-name"
          }

          "have the trading name text field with the pre-populated value" in {
            element(Selectors.tradingNameField).attr("value") shouldBe testTradingName
          }

          "have the continue button" in {
            elementText(Selectors.continueButton) shouldBe "Continue"
          }

          "show the remove trading name link" in {
            elementText(Selectors.removeTradingName) shouldBe "Remove trading name"
          }

          "have the correct remove trading name link" in {
            element(Selectors.removeTradingName).attr("href") shouldBe
              controllers.tradingName.routes.ConfirmRemoveTradingNameController.show().url
          }
        }

        "the user has no trading name in ETMP" should {
          lazy val view: Html = injectedView(tradingNameForm(testTradingName), "")
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the trading name text field with no pre-populated value" in {
            element(Selectors.tradingNameField).attr("value") shouldBe ""
          }

          "not show the remove trading name link" in {
            elementExtinct(Selectors.removeTradingName)
          }
        }
      }

      "the form has errors" should {

        lazy val view: Html = injectedView(tradingNameForm(testTradingName)
          .bind(Map("trading-name" -> testTradingName)), "")(user, messages, mockConfig)

        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Error: What is the trading name? - Business tax account - GOV.UK"
        }

        "display the error summary" which {

          "has the correct text" in {
            elementText(Selectors.errorSummaryTitle) shouldBe "There is a problem"
          }

          "has the correct link" in {
            element(Selectors.errorSummaryLink).attr("href") shouldBe "#trading-name"
          }
        }
      }
    }

    "the user is an agent" should {

      "there are no errors in the form" should {
        val view = injectedView(tradingNameForm(testTradingName).fill(testTradingName),
          testTradingName)(agent, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "What is the trading name? - Your client’s VAT details - GOV.UK"
        }
      }
    }
  }
}

