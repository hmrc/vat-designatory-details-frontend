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

import play.core.PlayVersion
import play.sbt.routes.RoutesKeys
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.SbtAutoBuildPlugin

val appName = "vat-designatory-details-frontend"

val govUkFrontendVersion       = "0.60.0-play-26"
val hmrcUkFrontendVersion      = "0.41.0-play-26"
val bootstrapPlayVersion       = "2.3.0"
val govTemplateVersion         = "5.63.0-play-26"
val playPartialsVersion        = "7.1.0-play-26"
val playUiVersion              = "8.21.0-play-26"
val playLanguageVersion        = "4.10.0-play-26"
val playWhiteListFilterVersion = "3.4.0-play-26"
val scalaTestPlusVersion       = "3.1.3"
val hmrcTestVersion            = "3.10.0-play-26"
val scalatestVersion           = "3.0.9"
val pegdownVersion             = "1.6.0"
val jsoupVersion               = "1.13.1"
val mockitoVersion             = "2.28.2"
val scalaMockVersion           = "3.6.0"
val wiremockVersion            = "2.27.2"
val playJsonJodaVersion        = "2.9.2"
val bootstrapFrontendVersion   = "3.4.0"

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty
RoutesKeys.routesImport := Seq.empty

lazy val coverageSettings: Seq[Setting[_]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    ".*standardError*.*",
    ".*govuk_wrapper*.*",
    ".*main_template*.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    "config.*",
    "testOnly.*",
    ".*feedback*.*",
    "views.*",
    "partials.*")

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimum := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

val compile = Seq(
  ws,
  "uk.gov.hmrc"       %% "bootstrap-frontend-play-26" % bootstrapFrontendVersion,
  "uk.gov.hmrc"       %% "govuk-template"             % govTemplateVersion,
  "uk.gov.hmrc"       %% "play-language"              % playLanguageVersion,
  "uk.gov.hmrc"       %% "play-ui"                    % playUiVersion,
  "uk.gov.hmrc"       %% "play-partials"              % playPartialsVersion,
  "uk.gov.hmrc"       %% "play-whitelist-filter"      % playWhiteListFilterVersion,
  "uk.gov.hmrc"       %% "play-frontend-govuk"        % govUkFrontendVersion,
  "uk.gov.hmrc"       %% "play-frontend-hmrc"         % hmrcUkFrontendVersion,
  "com.typesafe.play" %% "play-json-joda"             % playJsonJodaVersion
)

def test(scope: String = "test, it"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc"             %% "bootstrap-play-26"            % bootstrapPlayVersion  % scope classifier "tests",
  "uk.gov.hmrc"             %% "hmrctest"                     % hmrcTestVersion       % scope,
  "org.scalatest"           %% "scalatest"                    % scalatestVersion      % scope,
  "org.scalatestplus.play"  %% "scalatestplus-play"           % scalaTestPlusVersion  % scope,
  "org.scalamock"           %% "scalamock-scalatest-support"  % scalaMockVersion      % scope,
  "org.pegdown"             %  "pegdown"                      % pegdownVersion        % scope,
  "org.jsoup"               %  "jsoup"                        % jsoupVersion          % scope,
  "com.typesafe.play"       %% "play-test"                    % PlayVersion.current   % scope,
  "org.mockito"             %  "mockito-core"                 % mockitoVersion        % scope,
  "com.github.tomakehurst"  %  "wiremock-jre8"                % wiremockVersion       % scope
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests map {
  test =>
    Group(test.name, Seq(test), SubProcess(
      ForkOptions().withRunJVMOptions(Vector("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml"))
    ))
}

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.govukfrontend.views.html.helpers._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9165)
  .settings(coverageSettings: _*)
  .settings(playSettings: _*)
  .settings(majorVersion := 1)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    Keys.fork in Test := true,
    scalaVersion := "2.12.12",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false)
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo
  ))
