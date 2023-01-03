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

package views.businessName

import assets.UpdateBusinessNameViewMessages._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.businessName.BusinessNameChangeSuccessView

class BusinessNameChangeSuccessViewSpec extends ViewBaseSpec {

  val injectedView: BusinessNameChangeSuccessView = inject[BusinessNameChangeSuccessView]

  "The BusinessNameChangeSuccess view" when {

    "the user is an individual" should {

      lazy val view = injectedView()(messages, mockConfig, user)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct page title" in {
        document.title shouldBe pageTitleIndividual
      }

      "have the correct heading" in {
        elementText("h1") shouldBe viewPageTitle
      }

      "have the correct second heading" in {
        elementText("h2") shouldBe secondHeading
      }

      "have the correct first paragraph" in {
        elementText("#content p:nth-of-type(1)") shouldBe firstParagraph
      }

      "have the correct second paragraph" in {
        elementText("#content p:nth-of-type(2)") shouldBe secondParagraph
      }

      "have a finish button" which {

        "has the correct text" in {
          elementText(".govuk-button") shouldBe buttonTextIndividual
        }

        "has the correct href" in {
          element(".govuk-button").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
        }
      }
    }

    "the user is an agent" should {

      lazy val view = injectedView()(messages, mockConfig, agent)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct page title" in {
        document.title shouldBe pageTitleAgent
      }

      "have a finish button" which {

        "has the correct text" in {
          elementText(".govuk-button") shouldBe buttonTextAgent
        }

        "has the correct href" in {
          element(".govuk-button").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
        }

      }
    }
  }
}
