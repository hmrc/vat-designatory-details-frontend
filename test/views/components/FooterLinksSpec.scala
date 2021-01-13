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

package views.components

import utils.TestUtil

class FooterLinksSpec extends TestUtil {

  val footerLinks = new FooterLinks()

  "The FooterLinks component" should {

    "show the correct first footer item" which {

      "has the correct text" in {
        footerLinks.cookiesLink().text shouldBe Some("Cookies")
      }
      "has the correct link" in {
        footerLinks.cookiesLink().href shouldBe Some(mockConfig.footerCookiesUrl)
      }
    }

    "show the correct second footer item" which {

      "has the correct text" in {
        footerLinks.accessibilityLink().text shouldBe Some("Accessibility")
      }
      "has the correct link" in {
        footerLinks.accessibilityLink().href shouldBe Some(mockConfig.accessibilityLinkUrl)
      }
    }

    "show the correct third footer item" which {

      "has the correct text" in {
        footerLinks.privacyLink().text shouldBe Some("Privacy policy")
      }
      "has the correct link" in {
        footerLinks.privacyLink().href shouldBe Some(mockConfig.footerPrivacyUrl)
      }
    }

    "show the correct fourth footer item" which {

      "has the correct text" in {
        footerLinks.termsConditionsLink().text shouldBe Some("Terms and conditions")
      }
      "has the correct link" in {
        footerLinks.termsConditionsLink().href shouldBe Some(mockConfig.footerTermsConditionsUrl)
      }
    }

  }
}
