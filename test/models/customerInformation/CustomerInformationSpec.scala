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

package models.customerInformation

import assets.CustomerInfoConstants._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerInformationSpec extends AnyWordSpecLike with Matchers with OptionValues {


  "CustomerInformation" should {

    "parse from JSON" when {

      "all fields are present" in {
        val result = fullCustomerInfoJson.as[CustomerInformation]
        result shouldBe fullCustomerInfoModel
      }

      "the minimum number of fields are present" in {
        val result = minCustomerInfoJson.as[CustomerInformation]
        result shouldBe minCustomerInfoModel
      }
    }
  }

  ".sameTradingName" should {

    "return true when approved and pending trading name are the same" in {
      fullCustomerInfoModelSameTradingName.sameTradingName shouldBe true
    }

    "return false when approved and pending trading name are different" in {
      fullCustomerInfoModel.sameTradingName shouldBe false
    }
  }

  ".entityName" when {

    "the model contains a trading name" should {

      "return the trading name" in {
        val result: Option[String] = fullCustomerInfoModel.entityName
        result shouldBe Some("PepsiMac")
      }
    }

    "the model does not contain a trading name or organisation name" should {

      "return the first and last name" in {
        val customerInfoSpecific = fullCustomerInfoModel.copy(organisationName = None, tradingName = None)
        val result: Option[String] = customerInfoSpecific.entityName
        result shouldBe Some("Pepsi Mac")
      }
    }

    "the model does not contain a trading name, first name or last name" should {

      "return the organisation name" in {
        val customerInfoSpecific = fullCustomerInfoModel.copy(firstName = None, lastName = None, tradingName = None)
        val result: Option[String] = customerInfoSpecific.entityName
        result shouldBe Some("PepsiMac Ltd")
      }
    }

    "the model does not contain a trading name, organisation name, or individual names" should {

      "return None" in {
        val result: Option[String] = minCustomerInfoModel.entityName
        result shouldBe None
      }
    }
  }

  ".isValidPartyType" should {

    "return true if the value is in the list of valid types" in {
      fullCustomerInfoModel.isValidPartyType shouldBe true
    }

    "return false if the value is not in the list of valid types" in {
      minCustomerInfoModel.isValidPartyType shouldBe false
    }
  }

  "calling .isInsolventWithoutAccess" should {

    "return true when the insolvency type is 08, 09, 10 or 15" in {
      val blockedInsolvencyTestTypes: Seq[String] = Seq("08", "09", "10", "15")
      blockedInsolvencyTestTypes.foreach { iType =>
        customerInfoInsolvent.copy(continueToTrade = Some(true), insolvencyType = Some(iType)).isInsolventWithoutAccess shouldBe true
        customerInfoInsolvent.copy(continueToTrade = Some(false), insolvencyType = Some(iType)).isInsolventWithoutAccess shouldBe true
        customerInfoInsolvent.copy(continueToTrade = None, insolvencyType = Some(iType)).isInsolventWithoutAccess shouldBe true
      }

    }

    "return false when the insolvency type is 07, 12, 13 or 14" in {
      val allowedInsolvencyTestTypes: Seq[String] = Seq("07", "12", "13", "14")
      allowedInsolvencyTestTypes.foreach { iType =>
        customerInfoInsolvent.copy(continueToTrade = Some(true), insolvencyType = Some(iType)).isInsolventWithoutAccess shouldBe false
        customerInfoInsolvent.copy(continueToTrade = Some(false), insolvencyType = Some(iType)).isInsolventWithoutAccess  shouldBe false
        customerInfoInsolvent.copy(continueToTrade = None, insolvencyType = Some(iType)).isInsolventWithoutAccess  shouldBe false
      }
    }

    "when the insolvency type is not 07, 08, 09, 10, 12, 13, 14 or 15" should {

      "return true when the user is insolvent and not continuing to trade" in {
        customerInfoInsolvent.isInsolventWithoutAccess shouldBe true
      }

      "return false when the user is insolvent but is continuing to trade" in {
        customerInfoInsolvent.copy(continueToTrade = Some(true)).isInsolventWithoutAccess shouldBe false
      }

      "return false when the user is insolvent and doesn't have a continueToTrade value" in {
        customerInfoInsolvent.copy(continueToTrade = None).isInsolventWithoutAccess shouldBe false
      }
    }

    "return false when the user is not insolvent, regardless of the continueToTrade flag" in {
      fullCustomerInfoModel.isInsolventWithoutAccess shouldBe false
      fullCustomerInfoModel.copy(continueToTrade = Some(false)).isInsolventWithoutAccess shouldBe false
      fullCustomerInfoModel.copy(continueToTrade = None).isInsolventWithoutAccess shouldBe false
    }
  }
}
