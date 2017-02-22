#!/usr/bin/env groovy
import hudson.tasks.test.AbstractTestResultAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

def getTime(long start, long end) {

    long duration = end - start

    long allSeconds = duration / 1000
    int mins = allSeconds / 60
    int secs = allSeconds % 60

    StringBuffer sb = new StringBuffer()
    if (mins) {
        sb.append(mins)
        if (mins > 1) sb.append(' mins ')
        else sb.append(' min ')
    }

    if (secs) {
        sb.append(secs)
        if (secs > 1) sb.append(' secs ')
        else sb.append(' sec ')
    }

    sb.toString().trim()
}


def notifyBuild() {
    // build status of null means successful
    buildStatus = currentBuild.result ?: 'SUCCESS'

    String baseName = env.JOB_BASE_NAME
    baseName = baseName.replaceAll(/%2F/, '/')

    def colour = 'good'
    switch (buildStatus) {
        case 'UNSTABLE':
            colour = 'warning'
            break
        case 'FAILURE':
            colour = 'danger'
    }

    def statusString = buildStatus.toLowerCase().capitalize()
    def timeString = getTime(currentBuild.startTimeInMillis, System.currentTimeMillis())

    // Default values
    def message = "Mercury [${baseName}] - #${env.BUILD_NUMBER} ${statusString} (<${env.BUILD_URL}|Open>)\n" +
                  "Time: ${timeString}\n" +
                  "${getTestResults()}"

    // Send notifications
    slackSend(color: colour, message: message)
    slackSend(channel: '#development', color: colour, message: message, teamDomain: 'nhsdigitalssdgenomics', token: 'X1DrnvdZfv5ZF4qeuE9Gj5TN')
}


def getTestResults() {
    String results = "Test Status: Unknown"
    try {
        AbstractTestResultAction testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
        if (testResultAction != null) {
            def passed = testResultAction.totalCount - (testResultAction.failCount + testResultAction.skipCount)
            results = "Test Status:\n" +
                      "\tPassed: ${passed}, " +
                      "Failed: ${testResultAction.failCount}${testResultAction.failureDiffString}, " +
                      "Skipped: ${testResultAction.skipCount}"
        }
    } catch (Exception ignored) {}
    echo "${results}"
    results
}

Map getDbmUpdateJobs(String gradle) {
    Map jobs = [failFast: true]
    Path workspace = Paths.get(pwd() as String)

    Files.walk(workspace)
            .filter({path -> Files.exists(path.resolve('gradle.properties'))})
            .filter({path ->
        Files.readAllLines(path.resolve('gradle.properties')).any {line ->
            line.startsWith('dataSource')
        }
    }).each {project ->
        String dirName = project.fileName.toString()
        jobs["${dirName}"] = {
            dir("${dirName}") {
                sh "${gradle} dbmUpdate"
            }
        }
    }
    jobs
}

Map getIntegrationTestJobs(String gradle) {
    Map jobs = [failFast: true]
    Path workspace = Paths.get(pwd() as String)

    Files.walk(workspace)
            .filter({path -> Files.exists(path.resolve('src/integration-test'))})
            .each {project ->
        String dirName = project.fileName.toString()
        jobs["${dirName}"] = {
            dir("${dirName}") {
                sh "${grails} test-app --integration"
            }
        }
    }
    jobs
}