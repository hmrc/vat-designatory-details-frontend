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

package views.businessName

import common.ContactPreference
import models.viewModels.ChangeSuccessViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.businessName.BusinessNameChangeSuccessView

class BusinessNameChangeSuccessViewSpec extends ViewBaseSpec {

  val injectedView: BusinessNameChangeSuccessView = inject[BusinessNameChangeSuccessView]
  val exampleTitle = "ExampleTitle"

  "The BusinessNameChangeSuccess view" when {

    "the user is an individual" when {

      "their contact preference is digital" should {

        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, Some(ContactPreference.digital))
        lazy val view = injectedView(viewModel)(messages, mockConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          document.title shouldBe s"$exampleTitle - Business tax account - GOV.UK"
        }

        "have the heading provided by the mdoel" in {
          elementText("h1") shouldBe exampleTitle
        }

        "have the correct second heading" in {
          elementText("h2") shouldBe "What happens next"
        }

        "have the correct first paragraph" in {
          elementText("#content p:nth-of-type(1)") shouldBe "We’ll send you an email within 2 working days, " +
            "telling you whether we can accept the new business name."
        }

        "have the correct second paragraph" in {
          elementText("#content p:nth-of-type(2)") shouldBe "You can also check your HMRC secure messages for an update."
        }

        "have a finish button" which {

          "has the correct text" in {
            elementText(".govuk-button") shouldBe "View your business details"
          }

          "has the correct href" in {
            element(".govuk-button").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }
        }
      }

      "their contact preference is paper" should {

        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, Some(ContactPreference.paper))
        lazy val view = injectedView(viewModel)(messages, mockConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText("#content p:nth-of-type(1)") shouldBe "We’ll send a letter to your principal place of business " +
            "within 15 working days, telling you whether we can accept the new business name."
        }
      }

      "their contact preference could not be found" should {

        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, None)
        lazy val view = injectedView(viewModel)(messages, mockConfig, user)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText("#content p:nth-of-type(1)") shouldBe "We will send you an update within 15 working days."
        }
      }
    }

    "the user is an agent" when {

      "they have an email address in session (digital pref)" should {

        val viewModel = ChangeSuccessViewModel(exampleTitle, Some("lewis@email.com"), Some("Coca Cola"), None)
        lazy val view = injectedView(viewModel)(messages, mockConfig, agent)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          document.title shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
        }

        "have the correct first paragraph" in {
          elementText("#content p:nth-of-type(1)") shouldBe "We’ll send an email to lewis@email.com " +
            "within 2 working days, telling you whether we can accept the new business name."
        }

        "have the correct second paragraph" in {
          elementText("#content p:nth-of-type(2)") shouldBe "We’ll also contact your client with an update."
        }

        "have a finish button" which {

          "has the correct text" in {
            elementText(".govuk-button") shouldBe "View your client’s business details"
          }

          "has the correct href" in {
            element(".govuk-button").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }
        }
      }

      "they have no email address in session (no pref)" should {

        val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some("Coca Cola"), None)
        lazy val view = injectedView(viewModel)(messages, mockConfig, agent)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText("#content p:nth-of-type(1)") shouldBe "We’ll contact your client within 15 working days, " +
            "telling them whether we can accept your request to change the business name."
        }
      }
    }
  }
}
