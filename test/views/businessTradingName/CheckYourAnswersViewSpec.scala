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

package views.businessTradingName

import models.viewModels.CheckYourAnswersViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import views.ViewBaseSpec
import views.html.businessTradingName.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewBaseSpec {

  lazy val injectedView: CheckYourAnswersView = inject[CheckYourAnswersView]

  object Selectors {
    val heading = "h1"
    val newTradingName = ".govuk-summary-list__value"
    val firstColumn = ".govuk-summary-list__key"
    val changeLink = ".govuk-summary-list__actions > a"
    val changeLinkText = ".govuk-summary-list__actions > a > span:nth-child(1)"
    val changeLinkHiddenText = ".govuk-summary-list__actions > a > span:nth-child(2)"
    val confirmButton = ".govuk-button"
  }

  val viewModel = CheckYourAnswersViewModel("journeyName", "answer", "/change-link", "Change the answer", Call("POST", "/continue-link"))

  "Rendering the Check Your Answer view" when {

    "the user is a principle entity" should {

      lazy val view = injectedView(viewModel)(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe "Check your answer - Manage your VAT account - GOV.UK"
      }

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe "Check your answer"
      }

      "have the correct key text in the Check Your Answers table from the viewModel" in {
      elementText(Selectors.firstColumn) shouldBe "journeyName"
      }

      "have the trading name the user provided from the viewModel" in {
        elementText(Selectors.newTradingName) shouldBe "answer"
      }

      "have a link to change the trading name" which {

        "has the correct text" in {
          elementText(Selectors.changeLinkText) shouldBe "Change"
        }

        "has the correct URL from the viewModel" in {
          element(Selectors.changeLink).attr("href") shouldBe "/change-link"
        }

        "has hidden text" in {
          elementText(Selectors.changeLinkHiddenText) shouldBe "Change the answer"
        }
      }

      "have a form with the correct action" in {
        element("form").attr("action") shouldBe "/continue-link"
      }

      "have a continue button" which {

        "has the correct text" in {
          elementText(Selectors.confirmButton) shouldBe "Confirm and continue"
        }

        "has the prevent double click attribute" in {
          element(Selectors.confirmButton).hasAttr("data-prevent-double-click") shouldBe true
        }
      }
    }

    "the user is an agent" when {

      "there are no errors in the form" should {
        val view = injectedView(viewModel)(messages, mockConfig,  agent)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Check your answer - Your client’s VAT details - GOV.UK"
        }
      }
    }
  }
}
