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

import forms.BusinessNameForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.businessName.CaptureBusinessNameView

class CaptureBusinessNameViewSpec extends ViewBaseSpec {

  val injectedView: CaptureBusinessNameView = inject[CaptureBusinessNameView]

  object Selectors {
    val pageHeading = "#content h1"
    val backLink = ".govuk-back-link"
    val form = "form"
    val businessNameField = "#business-name"
    val continueButton = ".govuk-button"
    val errorSummary = "#business-name-error"
    val errorSummaryTitle = "#error-summary-title"
    val errorSummaryLink = ".govuk-error-summary__list > li > a"
    val businessNameFormGroup = "#content > article > form > div:nth-child(1)"
    val paragraphOne = "#content p:nth-of-type(1)"
    val paragraphTwo = "#content p:nth-of-type(2)"
    val businessGuidanceLink = "#content p:nth-of-type(1) a"

    def listItem(num: Int): String = s"#content > div > ul > li:nth-child($num)"
  }

  "Rendering the capture trading name page" when {

    "the user is a principle entity" when {

      "the form has no errors" should {

        lazy val view: Html = injectedView(businessNameForm(testBusinessName).fill(testBusinessName))(user, messages, mockConfig)

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "Change the business name - Business tax account - GOV.UK"
        }

        "have a back link" which {

          "should have the correct text" in {
            elementText(Selectors.backLink) shouldBe "Back"
          }

          "should have the correct href" in {
            element(Selectors.backLink).attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }
        }

        "have the correct page heading" in {
          elementText(Selectors.pageHeading) shouldBe "Change the business name"
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "You can check the guidance around business names (opens in a new tab) before you continue."
        }

        "have the correct make a payment link text in the first paragraph" in {
          elementText(Selectors.businessGuidanceLink) shouldBe "check the guidance around business names (opens in a new tab)"
        }

        "have the correct href" in {
          element(Selectors.businessGuidanceLink).attr("href") shouldBe
            mockConfig.businessNameGuidanceUrl
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "In general, business names cannot:"
        }

        "have a list" which {

          "has the correct first item" in {
            elementText(Selectors.listItem(1)) shouldBe "be longer than 160 characters"
          }
          "has the correct second item" in {
            elementText(Selectors.listItem(2)) shouldBe "contain certain special characters or symbols"
          }
          "has the correct third item" in {
            elementText(Selectors.listItem(3)) shouldBe "contain offensive language"
          }

        }

        "have the business name form with the correct form action" in {
          element(Selectors.form).attr("action") shouldBe "/vat-through-software/account/designatory/change-business-name"
        }

        "have the business name text field with the pre-populated value" in {
          element(Selectors.businessNameField).attr("value") shouldBe testBusinessName
        }

        "have the continue button" in {
          elementText(Selectors.continueButton) shouldBe "Continue"
        }
      }

      "the form has errors" should {

        lazy val view: Html = injectedView(businessNameForm(testBusinessName)
          .bind(Map("business-name" -> testBusinessName)))(user, messages, mockConfig)

        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Error: Change the business name - Business tax account - GOV.UK"
        }

        "display the error summary" which {

          "has the correct text" in {
            elementText(Selectors.errorSummaryTitle) shouldBe "There is a problem"
          }

          "has the correct link" in {
            element(Selectors.errorSummaryLink).attr("href") shouldBe "#business-name"
          }
        }
      }

      "the user is an agent" should {

        "there are no errors in the form" should {
          val view = injectedView(businessNameForm(testBusinessName).fill(testBusinessName))(agent, messages, mockConfig)
          implicit val document: Document = Jsoup.parse(view.body)

          "have the correct title" in {
            document.title shouldBe "Change the business name - Your client’s VAT details - GOV.UK"
          }
        }
      }
    }
  }
}
