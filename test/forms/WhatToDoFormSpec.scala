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

package forms

import forms.WhatToDoForm.{change, changeRemove, remove, whatToDoForm}
import models.{Change, Remove}
import play.api.data.FormError
import utils.TestUtil

class WhatToDoFormSpec extends TestUtil {

  "The WhatToDoForm" should {

    "validate that 'change' is valid" in {
      val actual = whatToDoForm.bind(Map(changeRemove -> change)).value
      actual shouldBe Some(Change)
    }

    "validate that 'remove' is valid" in {
      val actual = whatToDoForm.bind(Map(changeRemove -> remove)).value
      actual shouldBe Some(Remove)
    }

    "validate that data has been entered" in {
      val formWithError = whatToDoForm.bind(Map[String,String]())
      formWithError.errors should contain(FormError(changeRemove, "whatToDo.error"))
    }

    "unbind the object 'Change'" in {
      WhatToDoForm.formatter.unbind(changeRemove, Change) shouldBe Map(changeRemove -> change)
    }

    "unbind the object 'Remove'" in {
      WhatToDoForm.formatter.unbind(changeRemove, Remove) shouldBe Map(changeRemove -> remove)
    }
  }
}
