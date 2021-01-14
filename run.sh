#!/usr/bin/env bash
sbt 'run 9165 -Dlogger.resource=logback-test.xml -Dplay.http.router=testOnly.Routes'
