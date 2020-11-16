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
import common.ContactPreference
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
    val paragraphOne = "#content p:nth-of-type(1)"
    val paragraphTwo = "#content p:nth-of-type(2)"
    val paragraphThree = "#content p:nth-of-type(3)"
    val button = ".govuk-button"
  }

  val exampleTitle = "ExampleTitle"

  "The Change Successful view" when {

    "an individual is adding a trading name" when {

      "the contact preference is digital" when {

        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, Some(ContactPreference.digital), false)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111"))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          elementText(Selectors.title) shouldBe s"$exampleTitle - Business tax account - GOV.UK"
        }

        "have the heading provided by the model" in {
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
          elementText(Selectors.paragraphOne) shouldBe "We’ll send you an email within 2 working days, telling you whether we can accept the trading name."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "You can also check your secure messages for an update."
        }
      }

      "the contact preference is paper" when {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, Some(ContactPreference.paper), false)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111"))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We’ll send a letter to your principal place of business " +
            "within 15 working days, telling you whether we can accept the trading name."
        }
      }

      "the user has no contact preference" when {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, None, false)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111"))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We will send you an update within 15 working days."
        }
      }
    }

    "an individual is removing the trading name" when {

      "the contact preference is digital" when {

        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, Some(ContactPreference.digital), true)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111"))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We’ll send you an email within 2 working days, " +
            "to confirm that the trading name has been removed from your VAT account."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "A trading name will no longer be listed on your VAT certificate."
        }
      }

      "the contact preference is paper" when {

        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, Some(ContactPreference.paper), true)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111"))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We’ll send a letter to your principal place of business within 15 working days, " +
            "to confirm that the trading name has been removed from your VAT account."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "A trading name will no longer be listed on your VAT certificate."
        }
      }
    }

    "an agent is adding a trading name" when {

      "the agent has an email address registered" when {

        val viewModel = ChangeSuccessViewModel(exampleTitle, Some("agent@example.com"), Some("TheBusiness"), Some("Digital"), false)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          elementText(Selectors.title) shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
        }

        "have a finish button with the correct text" in {
          elementText(Selectors.button) shouldBe "View your client’s business details"
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We’ll send you an email within 2" +
            " working days, telling you whether we can accept the trading name."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "We’ll also contact TheBusiness with an update."
        }
      }

      "the agent doesn't have an email address registered" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some("TheBusiness"), None, false)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe
            "We’ll update TheBusiness telling them we have added the trading name."
        }
      }

      "the client's business name isn't retrieved" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, None, false)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe
            "We’ll update your client telling them we have added the trading name."
        }
      }
    }

    "an agent is removing a trading name" when {

      "the agent has an email address registered" when {

        val viewModel = ChangeSuccessViewModel(exampleTitle, Some("agent@example.com"), Some("TheBusiness"), Some("Digital"), true)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We’ll send you an email within 2" +
            " working days, to confirm that the trading name has been removed from your client’s VAT account."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "We’ll also contact TheBusiness with an update."
        }

        "have the correct third paragraph" in {
          elementText(Selectors.paragraphThree) shouldBe "A trading name will no longer be listed on the VAT certificate."
        }
      }

      "the agent doesn't have an email address registered" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some("TheBusiness"), None, true)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe
            "We’ll update TheBusiness telling them we have removed the trading name."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "A trading name will no longer be listed on the VAT certificate."
        }
      }

      "the client's business name isn't retrieved" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, None, true)
        lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe
            "We’ll update your client telling them we have removed the trading name."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "A trading name will no longer be listed on the VAT certificate."
        }
      }
    }
  }
}
