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

package views.errors

import assets.InFlightViewMessages
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.InFlightChangeView

class InFlightChangeViewSpec extends ViewBaseSpec {

  val injectedView: InFlightChangeView = inject[InFlightChangeView]
  val viewMessages: InFlightViewMessages.type = assets.InFlightViewMessages

  object Selectors {
    val heading = "h1"
    val paragraphOne = ".govuk-body:nth-of-type(1)"
    val paragraphTwo = ".govuk-body:nth-of-type(2)"
    val backLink = ".govuk-back-link"
    val accountDetailsBack = "#content .govuk-link"

    def listItem(num: Int): String = s"#content > ul > li:nth-child($num)"
  }

  "The Inflight change pending view" when {

    "the pending change is trading name" when {

      "the user is a principal entity" should {

        lazy val view = injectedView()(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe viewMessages.title
        }

        "have the correct heading" in {
          elementText(Selectors.heading) shouldBe viewMessages.heading
        }

        "have the correct first paragraph text" in {
          elementText(Selectors.paragraphOne) shouldBe viewMessages.para1
        }

        "have the correct second paragraph text" in {
          elementText(Selectors.paragraphTwo) shouldBe viewMessages.para2
        }

        "have the correct text for the back link" in {
          elementText(Selectors.backLink) shouldBe viewMessages.backLink
        }

        "have the correct back link location" in {
          element(Selectors.backLink).attr("href") shouldBe "mockManageVatOverviewUrl"
        }

        "have a list" which {

          "has the correct first item" in {
            elementText(Selectors.listItem(1)) shouldBe viewMessages.list1
          }
          "has the correct second item" in {
            elementText(Selectors.listItem(2)) shouldBe viewMessages.list2
          }
          "has the correct third item" in {
            elementText(Selectors.listItem(3)) shouldBe viewMessages.list3
          }

        }

        "have the correct link back to the account details" which {

          "has the correct URL" in {
            element(Selectors.accountDetailsBack).attr("href") shouldBe
              mockConfig.manageVatSubscriptionServicePath
          }

          "has the correct text" in {
            elementText(Selectors.accountDetailsBack) shouldBe viewMessages.accountDetailsLinkNonAgent
          }
        }

      }

      "the user is an agent" should {
        lazy val view = injectedView()(agent, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe viewMessages.titleAgent
        }

        "have the correct back to account details link text" in {
          elementText(Selectors.accountDetailsBack) shouldBe viewMessages.accountDetailsLinkAgent
        }
      }
    }
  }
}
