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

package views.templates

import assets.BaseTestConstants
import models.User
import models.viewModels.ChangeSuccessViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.templates.ChangeSuccessView

class ChangeSuccessViewSpec extends ViewBaseSpec {

  val injectedView: ChangeSuccessView = inject[ChangeSuccessView]

  object Selectors {
    val title = "title"
    val pageHeading = "h1"
    val secondaryHeading = "h2"
    val paragraphOne = "#content article p:nth-of-type(1)"
    val paragraph = "#content article p:nth-of-type(1)"
    val button = "#content > article > a"
  }

  val exampleTitle = "ExampleTitle"

  "The Change Successful view" when {

    "an individual is performing the action" when {

      val viewModel = ChangeSuccessViewModel(exampleTitle, None, None)
      lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111"))

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the page title provided by the model" in {
        elementText(Selectors.title) shouldBe s"$exampleTitle - Business tax account - GOV.UK"
      }

      "have the heading provided by the model" in {
        elementText(Selectors.pageHeading) shouldBe exampleTitle
      }

      "have a finish button with the correct text" in {
        elementText(Selectors.button) shouldBe "Finish"
      }

      "have a finish button which navigates to the Change of Circs overview page" in {
        element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
      }

      "have the correct paragraph" in {
        elementText(Selectors.paragraph) shouldBe "Make sure your contact details are up to date."
      }
    }

    "an agent is performing the action" when {

      "the agent has an email address registered" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, Some("agent@example.com"), Some("TheBusiness"))
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          elementText(Selectors.title) shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
        }

        "have the heading provided by the model" in {
          elementText(Selectors.pageHeading) shouldBe exampleTitle
        }

        "have a finish button with the correct text" in {
          elementText(Selectors.button) shouldBe "Finish"
        }

        "have a finish button which navigates to the Change of Circs overview page" in {
          element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We’ll send an email to agent@example.com within 2" +
            " working days telling you whether or not the request has been accepted."
        }
      }

      "the agent doesn't have an email address registered" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some("TheBusiness"))
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          elementText(Selectors.title) shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
        }

        "have the heading provided by the model" in {
          elementText(Selectors.pageHeading) shouldBe exampleTitle
        }

        "have a finish button with the correct text" in {
          elementText(Selectors.button) shouldBe "Finish"
        }

        "have a finish button which navigates to the Change of Circs overview page" in {
          element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe
            "We’ll send a confirmation letter to the agency address registered with HMRC within 15 working days."
        }
      }

      "the client's business name isn't retrieved" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          elementText(Selectors.title) shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
        }

        "have the heading provided by the model" in {
          elementText(Selectors.pageHeading) shouldBe exampleTitle
        }

        "have a finish button with the correct text" in {
          elementText(Selectors.button) shouldBe "Finish"
        }

        "have a finish button which navigates to the Change of Circs overview page" in {
          element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
        }

      }
    }
  }
}
