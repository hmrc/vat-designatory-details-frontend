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

import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}

val appName = "vat-designatory-details-frontend"

val hmrcUkFrontendVersion    = "9.11.0"
val pegdownVersion           = "1.6.0"
val jsoupVersion             = "1.19.1"
val mockitoVersion           = "3.2.10.0"
val scalaMockVersion         = "7.3.0"
val wiremockVersion          = "3.0.1"
val playJsonJodaVersion      = "2.10.6"
val bootstrapFrontendVersion = "8.6.0"

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
lazy val plugins: Seq[Plugins]          = Seq.empty
lazy val playSettings: Seq[Setting[?]]  = Seq.empty
scalacOptions ++= Seq(
  "-Wconf:cat=unused-imports&site=.*views.html.*:s",
  "-Wconf:cat=unused-imports&src=routes/.*:s",
  "-Wconf:cat=unused&src=routes/.*:s"
)
RoutesKeys.routesImport := Seq.empty

lazy val coverageSettings: Seq[Setting[?]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    "app.*",
    "prod.*",
    "config.*",
    "views.*"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum    := true,
    ScoverageKeys.coverageHighlighting     := true
  )
}

val compile = Seq(
  ws,
  "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapFrontendVersion,
  "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % hmrcUkFrontendVersion,
  "com.typesafe.play"            %% "play-json-joda"             % playJsonJodaVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.15.2"
)

def test(scope: String = "test, it"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc"           %% "bootstrap-test-play-30" % bootstrapFrontendVersion % scope,
  "org.scalamock"         %% "scalamock"              % scalaMockVersion         % scope,
  "org.pegdown"            % "pegdown"                % pegdownVersion           % scope,
  "org.jsoup"              % "jsoup"                  % jsoupVersion             % scope,
  "org.scalatestplus"     %% "mockito-3-4"            % mockitoVersion           % scope,
  "com.github.tomakehurst" % "wiremock"               % wiremockVersion          % scope
)

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9165)
  .settings(coverageSettings *)
  .settings(playSettings *)
  .settings(majorVersion := 1)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    Test / Keys.fork := true,
    scalaVersion     := "2.13.16",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings) *)
  .settings(
    IntegrationTest / Keys.fork                  := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false
  )
