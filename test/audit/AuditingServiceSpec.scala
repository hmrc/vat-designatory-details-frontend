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

package audit

import audit.models.AuditModel
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import utils.TestUtil

import scala.concurrent.{ExecutionContext, Future}

class AuditingServiceSpec extends TestUtil with MockitoSugar {

  val appName: String = "vat-designatory-details-frontend"
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val testAuditingService = new AuditingService(mockAuditConnector)
  val testPath = "/test/path"

  "AuditingService" when {

    "calling the referrer method" should {

      "extract the referrer if there is one" in {
        testAuditingService.referrer(HeaderCarrier().withExtraHeaders(HeaderNames.REFERER -> testPath)) shouldBe testPath
      }

      "default to hyphen '-' if there is no referrer" in {
        testAuditingService.referrer(HeaderCarrier()) shouldBe "-"
      }
    }

    "given an AuditModel" should {

      val testModel = new AuditModel {
        override val transactionName: String = "test-trans-name"
        override val auditType: String = "test-audit-type"
        override val detail: JsValue = Json.obj("foo" -> "bar")
      }

      val expectedData = testAuditingService.toExtendedDataEvent(appName, testModel, testPath)

      when(mockAuditConnector.sendExtendedEvent(
        refEq(expectedData, "eventId", "generatedAt"))(any[HeaderCarrier], any[ExecutionContext])
      ).thenReturn(Future.successful(AuditResult.Success))

      "pass it to the AuditConnector" in {
        testAuditingService.audit(testModel, Some(testPath))

        verify(mockAuditConnector).sendExtendedEvent(
          refEq(expectedData, "eventId", "generatedAt"))(any[HeaderCarrier], any[ExecutionContext])
      }
    }
  }
}
