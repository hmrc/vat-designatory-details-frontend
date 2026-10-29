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

package views

import config.ConfigKeys
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import uk.gov.hmrc.hmrcfrontend.views.Aliases.UserResearchBanner
import views.helpers.UrBannerUtil
import views.html.MainTemplate

class MainTemplateSpec extends ViewBaseSpec {

  val injectedView: MainTemplate = inject[MainTemplate]
  val userBannerHeading: String = "Help make GOV.UK better"
  lazy val urBanner: UserResearchBanner = UrBannerUtil.getUrBanner()(mockConfig, messages)
  lazy val urBannerWithCloseButton: UserResearchBanner = UrBannerUtil.getUrBanner(hideCloseButton = false)(mockConfig, messages)

  override def afterEach(): Unit = {
    sys.props -= ConfigKeys.showUserResearchBannerEnabled
    super.afterEach()
  }

  object Selectors {
    val navTitle = ".govuk-service-navigation__link"
    val navTitleNoUserType = ".govuk-service-navigation__text"
    val signOutLink = ".hmrc-sign-out-nav__link"
    val firstListLink = "li > a"
    val userResearchBanner = ".hmrc-user-research-banner"
    val userResearchBannerHeading = ".hmrc-user-research-banner__title"
    val userResearchBannerCloseButton = ".hmrc-user-research-banner__close"
  }

  "The main template" when {

    "it is not given a service name" when {

      "the user is an individual" should {

        lazy val view = injectedView(pageTitle = "Test")(Html("Test"))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "show the correct page title" in {
          elementText(Selectors.navTitle) shouldBe "Manage your VAT account"
        }

        "have the correct service URL" in {
          element(Selectors.navTitle).attr("href") shouldBe mockConfig.vatSummaryFrontendUrl
        }
      }

      "the user is an agent" should {
        lazy val view = injectedView(pageTitle = "Test")(Html("Test"))(agent, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "show the correct page title" in {
          elementText(Selectors.navTitle) shouldBe "Your client’s VAT details"

        }

        "have the correct service URL" in {
          element(Selectors.navTitle).attr("href") shouldBe mockConfig.vatAgentClientLookupHubPath
        }
      }

        "the user is not known" should {
          lazy val view = injectedView(pageTitle = "Test")(Html("Test"))(postRequest, messages, mockConfig)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have no nav title" in {
            elementText(Selectors.navTitleNoUserType) shouldBe "VAT"
          }

          "have the correct service URL" in {
            element(Selectors.navTitleNoUserType).attr("href") shouldBe ""
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

    "the showUserResearchBanner feature switch is turned off" when {

      "a user research banner is provided" should {

        lazy val view = injectedView(pageTitle = "Test", urBanner = Some(urBanner))(Html("Test"))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "not display the user research banner" in {
          mockConfig.features.showUserResearchBannerEnabled(false)
          elementExtinct(Selectors.userResearchBanner)
        }
      }
    }

    "the showUserResearchBanner feature switch is turned on" when {

      "a user research banner without a close button is provided" should {

        lazy val view = injectedView(pageTitle = "Test", urBanner = Some(urBanner))(Html("Test"))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the correct user research banner heading" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          elementText(Selectors.userResearchBannerHeading) shouldBe userBannerHeading
        }

        "display the user research banner link with the configured url" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          element(s"${Selectors.userResearchBanner} a").attr("href") shouldBe mockConfig.urBannerUrl
        }

        "not display the close button" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          elementExtinct(Selectors.userResearchBannerCloseButton)
        }
      }

      "a user research banner with a close button is provided" should {

        lazy val view = injectedView(pageTitle = "Test", urBanner = Some(urBannerWithCloseButton))(Html("Test"))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the close button" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          elementExists(Selectors.userResearchBannerCloseButton)
        }
      }

      "no user research banner is provided" should {

        lazy val view = injectedView(pageTitle = "Test")(Html("Test"))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "not display the user research banner" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          elementExtinct(Selectors.userResearchBanner)
        }
      }
    }
  }
}
