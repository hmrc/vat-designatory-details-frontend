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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.MainTemplate

class MainTemplateSpec extends ViewBaseSpec {

  val injectedView: MainTemplate = inject[MainTemplate]

  object Selectors {
    val navTitle = ".govuk-header__link--service-name"
    val signOutLink = ".hmrc-sign-out-nav__link"
    val firstListLink = "li > a"
  }

  "The main template" when {

    "it is not given a service name" when {

      "the user is an individual" should {

        lazy val view = injectedView(pageTitle = "Test")(Html("Test"))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "show the correct page title" in {
          elementText(Selectors.navTitle) shouldBe "Business tax account"
        }
      }

      "the user is an agent" should {
        lazy val view = injectedView(pageTitle = "Test")(Html("Test"))(agent, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "show the correct page title" in {
          elementText(Selectors.navTitle) shouldBe "Your client’s VAT details"

        }

        "the user is not known" should {
          lazy val view = injectedView(pageTitle = "Test")(Html("Test"))(request, messages, mockConfig)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have no nav title" in {
            elementText(Selectors.navTitle) shouldBe "VAT"
          }
        }
      }
    }

    "given a service name" should {

      lazy val view = injectedView(pageTitle = "Test", serviceName = Some("Test"))(Html("Test"))(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "show the correct page title and service name" in {
        document.title shouldBe "Test - Test - GOV.UK"
      }
    }

    "showSignOut is set to true" should {
      lazy val view = injectedView(pageTitle = "Test", showSignOut = true)(Html("Test"))(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "show the sign out link" in {
        elementText(Selectors.signOutLink) shouldBe "Sign out"
      }
    }

    "showSignOut is set to false" should {
      lazy val view = injectedView(pageTitle = "Test", showSignOut = false)(Html("Test"))(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not show the sign out link" in {
        elementText(Selectors.firstListLink) should not contain "Sign out"
      }
    }

    "feedbackOnSignOut is set to true" should {
      lazy val view = injectedView(pageTitle = "Test", feedbackOnSignOut = true)(Html("Test"))(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct signout URL" in {
        element(Selectors.signOutLink).attr("href") shouldBe
          "/vat-through-software/account/designatory/sign-out?feedbackOnSignOut=true"
      }
    }

    "feedbackOnSignOut is set to false" should {
      lazy val view = injectedView(pageTitle = "Test", feedbackOnSignOut = false)(Html("Test"))(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct signout URL" in {
        element(Selectors.signOutLink).attr("href") shouldBe
          "/vat-through-software/account/designatory/sign-out?feedbackOnSignOut=false"
      }
    }

  }
}
