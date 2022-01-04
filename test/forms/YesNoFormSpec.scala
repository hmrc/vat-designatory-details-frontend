/*
 * Copyright 2022 HM Revenue & Customs
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

package forms

import forms.YesNoForm.{yes, yesNo, yesNoForm}
import models.{No, Yes}
import play.api.data.FormError
import utils.TestUtil

class YesNoFormSpec extends TestUtil {

  "The YesNoForm" should {

    "validate that 'yes' is valid" in {
      val actual = yesNoForm("").bind(Map(yesNo -> yes)).value
      actual shouldBe Some(Yes)
    }

    "validate that 'no' is valid" in {
      val actual = yesNoForm("").bind(Map(yesNo -> YesNoForm.no)).value
      actual shouldBe Some(No)
    }

    "validate that data has been entered" in {
      val formWithError = yesNoForm("genericYesNoError").bind(Map[String,String]())
      formWithError.errors should contain(FormError(yesNo, "genericYesNoError"))
    }

    "unbind the object 'Yes'" in {
      YesNoForm.formatter("").unbind(yesNo, Yes) shouldBe Map(yesNo -> yes)
    }

    "unbind the object 'No'" in {
      YesNoForm.formatter("").unbind(yesNo, No) shouldBe Map(yesNo -> YesNoForm.no)
    }
  }

}
