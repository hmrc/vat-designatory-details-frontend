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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.tradingName.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewBaseSpec {

  lazy val injectedView: CheckYourAnswersView = inject[CheckYourAnswersView]

  object Selectors {
    val heading = "h1"
    val newTradingName = ".govuk-summary-list__value"
    val firstColumn = ".govuk-summary-list__key"
    val changeLink = "dd > a"
    val confirmButton = ".govuk-button"
  }

  "Rendering the Check Your Answer view" when {

    "the user is a principle entity" should {

      lazy val view = injectedView(testTradingName)(mockConfig, messages, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe "Check your answer - Business tax account - GOV.UK"
      }

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe "Check your answer"
      }

      "have the correct key text in the Check Your Answers table" in {
      elementText(Selectors.firstColumn) shouldBe "Trading name"
      }

      "have the trading name the user provided" in {
        elementText(Selectors.newTradingName) shouldBe testTradingName
      }

      "have a link to change the trading name" which {

        "has the correct text" in {
          elementText(Selectors.changeLink) shouldBe "Change"
        }

        "has the correct URL" in {
          element(Selectors.changeLink).attr("href") shouldBe
            "/vat-through-software/account/designatory/new-trading-name"
        }
      }

      "have a continue button" which {

        "has the correct text" in {
          elementText(Selectors.confirmButton) shouldBe "Confirm and continue"
        }

        "has the correct URL" in {
          element(Selectors.confirmButton).attr("href") shouldBe
            "/vat-through-software/account/designatory/update-trading-name"
        }

      }
    }

    "the user is an agent" when {

      "there are no errors in the form" should {
        val view = injectedView(testTradingName)(mockConfig, messages, agent)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Check your answer - Your client’s VAT details - GOV.UK"
        }
      }
    }
  }
}
