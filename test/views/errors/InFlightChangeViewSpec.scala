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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.InFlightChangeView

class InFlightChangeViewSpec extends ViewBaseSpec {

  val injectedView: InFlightChangeView = inject[InFlightChangeView]

  object Selectors {
    val heading = "h1"
    val paragraphOne = "article > p:nth-child(3)"
    val paragraphTwo = "article > p:nth-child(4)"
    val backLink = ".link-back"

    def listItem(num: Int): String = s"#content > article > ul > li:nth-child($num)"
  }

  "The Inflight change pending view" when {

    "the pending change is trading name" should {

      lazy val view = injectedView("tradingName")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe "You already have a change pending - VAT - GOV.UK"
      }

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe "You already have a change pending"
      }

      "have the correct text for the back link" in {
        elementText(Selectors.backLink) shouldBe "Back"
      }

      "have the correct back link location" in {
        element(Selectors.backLink).attr("href") shouldBe "mockManageVatOverviewUrl"
      }
    }
  }
}
