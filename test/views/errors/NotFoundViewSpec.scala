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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.NotFoundView

class NotFoundViewSpec extends ViewBaseSpec {

  val injectedView: NotFoundView = inject[NotFoundView]

  object Selectors {
    val heading = "#content h1"
    val instructions = "#content > p"
  }

  "Rendering the not found page" should {

    lazy val view = injectedView()(mockConfig, user, messages)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "Page not found - Manage your VAT account - GOV.UK"
    }

    "have the correct page heading" in {
      elementText(Selectors.heading) shouldBe "This page cannot be found"
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe "Please check that you have entered the correct web address."
    }
  }
}
