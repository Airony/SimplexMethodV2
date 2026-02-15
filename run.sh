#!/bin/sh

set -xe

mvn clean compile exec:java -Dexec.mainClass="test.BranchBound"