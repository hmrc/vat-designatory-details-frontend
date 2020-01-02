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

package models.customerInformation

import assets.CustomerInfoConstants._
import uk.gov.hmrc.play.test.UnitSpec

class CustomerInformationSpec extends UnitSpec {


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
}
