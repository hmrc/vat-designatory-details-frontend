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

package connectors.httpParsers

import models.errors.ErrorModel
import models.customerInformation.UpdateOrganisationDetailsSuccess
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerUtil

object UpdateOrganisationDetailsHttpParser extends LoggerUtil {

  type UpdateOrganisationDetailsResponse = Either[ErrorModel, UpdateOrganisationDetailsSuccess]

  implicit object UpdateOrganisationDetailsReads extends HttpReads[UpdateOrganisationDetailsResponse] {
    override def read(method: String, url: String, response: HttpResponse): UpdateOrganisationDetailsResponse = {
      response.status match {
        case OK => response.json.validate[UpdateOrganisationDetailsSuccess].fold(
          invalid => {
            logger.warn(s"[UpdateOrganisationDetailsHttpParser][read] - Invalid JSON: $invalid")
            Left(ErrorModel(INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON."))
          },
          valid => {
            logger.debug("[UpdateOrganisationDetailsHttpParser][read] - Successfully parsed update response.")
            Right(valid)
          }
        )
        case status =>
          logger.warn(
            s"[UpdateOrganisationDetailsHttpParser][read] - " +
              s"Unexpected Response, Status $status returned, with response: ${response.body}"
          )
          Left(ErrorModel(status, response.body))
      }
    }
  }

}
