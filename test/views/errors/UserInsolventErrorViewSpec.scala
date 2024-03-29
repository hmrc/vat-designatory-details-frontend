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

package views.errors

import assets.UserInsolventMessages
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.UserInsolventErrorView

class UserInsolventErrorSpec extends ViewBaseSpec {

  val userInsolvent: UserInsolventErrorView = inject[UserInsolventErrorView]

  "Rendering the insolvent view page" should {

    object Selectors {
      val pageHeading = "#insolvent-without-access-heading"
      val message = "#insolvent-without-access-body"
      val button = ".govuk-button"
      val signOutLink = "#sign-out-link"
    }

    lazy val view = userInsolvent()(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe UserInsolventMessages.title
    }

    "have a the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe UserInsolventMessages.heading
    }

    "have the correct body" in {
      elementText(Selectors.message) shouldBe UserInsolventMessages.message
    }

    "have a sign out link" in {
      element(Selectors.signOutLink).attr("href") shouldBe "/vat-through-software/account/designatory/sign-out?feedbackOnSignOut=false"
    }

    "have a sign out link with the correct text" in {
      elementText(Selectors.signOutLink) shouldBe UserInsolventMessages.signOutLink
    }

    "have the correct button text" in {
      elementText(Selectors.button) shouldBe UserInsolventMessages.buttonText
    }

    "have the correct button link" in {
      element(Selectors.button).attr("href") shouldBe "bta-url"
    }

  }

}