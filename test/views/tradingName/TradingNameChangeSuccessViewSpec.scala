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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.tradingName.TradingNameChangeSuccessView

class TradingNameChangeSuccessViewSpec extends ViewBaseSpec {

  val injectedView: TradingNameChangeSuccessView = inject[TradingNameChangeSuccessView]

  object Selectors {
    val title = "title"
    val pageHeading = "h1"
    val secondaryHeading = "h2"
    val paragraphOne = "#content p:nth-of-type(1)"
    val paragraphTwo = "#content p:nth-of-type(2)"
    val button = ".govuk-button"
  }

  val exampleTitle = "ExampleTitle"

  "The TradingNameChangeSuccess view" when {

    "an individual is adding a trading name" when {

      lazy val view = injectedView(exampleTitle, isRemoval = false)(messages, mockConfig, user)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct page title" in {
        elementText(Selectors.title) shouldBe s"$exampleTitle - Manage your VAT account - GOV.UK"
      }

      "have the correct heading" in {
        elementText(Selectors.pageHeading) shouldBe exampleTitle
      }

      "have the correct second heading" in {
        elementText(Selectors.secondaryHeading) shouldBe "What happens next"
      }

      "have a finish button with the correct text" in {
        elementText(Selectors.button) shouldBe "View your business details"
      }

      "have a finish button which navigates to the Change of Circs overview page" in {
        element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
      }

      "have the correct first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe "If we can accept your request, we’ll update the VAT account with the new trading name."
      }

      "have the correct second paragraph" in {
        elementText(Selectors.paragraphTwo) shouldBe
          "Updates are usually done within 2 working days. However, they can take several weeks if we need to carry out additional checks."
      }
    }

    "an individual is removing the trading name" when {

      lazy val view = injectedView(exampleTitle, isRemoval = true)(messages, mockConfig, user)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe "We will remove the trading name from the VAT account."
      }

      "have the correct second paragraph" in {
        elementText(Selectors.paragraphTwo) shouldBe
          "Updates are usually done within 2 working days. However, they can take several weeks if we need to carry out additional checks."
      }
    }

    "an individual is changing an existing trading name" when {

      lazy val view = injectedView(exampleTitle, isRemoval = false)(messages, mockConfig, user)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe "If we can accept your request, we’ll update the VAT account with the new trading name."
      }

      "have the correct second paragraph" in {
        elementText(Selectors.paragraphTwo) shouldBe
          "Updates are usually done within 2 working days. However, they can take several weeks if we need to carry out additional checks."
      }
    }

    "an agent is adding a trading name" when {

      lazy val view = injectedView(exampleTitle, isRemoval = false)(
        messages, mockConfig, agent
      )

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct page title" in {
        elementText(Selectors.title) shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
      }

      "have a finish button with the correct text" in {
        elementText(Selectors.button) shouldBe "View your client’s business details"
      }

      "have the correct first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe "If we can accept your request, we’ll update the VAT account with the new trading name."
      }

      "have the correct second paragraph" in {
        elementText(Selectors.paragraphTwo) shouldBe
          "Updates are usually done within 2 working days. However, they can take several weeks if we need to carry out additional checks."
      }
    }

    "an agent is removing a trading name" when {

      lazy val view = injectedView(exampleTitle, isRemoval = true)(
        messages, mockConfig, agent
      )

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe "We will remove the trading name from the VAT account."
      }

      "have the correct second paragraph" in {
        elementText(Selectors.paragraphTwo) shouldBe
          "Updates are usually done within 2 working days. However, they can take several weeks if we need to carry out additional checks."
      }
    }

    "an agent is changing an existing trading  name" when {

      lazy val view = injectedView(exampleTitle, isRemoval = false)(
        messages, mockConfig, agent
      )

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe "If we can accept your request, we’ll update the VAT account with the new trading name."
      }

      "have the correct second paragraph" in {
        elementText(Selectors.paragraphTwo) shouldBe
          "Updates are usually done within 2 working days. However, they can take several weeks if we need to carry out additional checks."
      }
    }
  }
}
