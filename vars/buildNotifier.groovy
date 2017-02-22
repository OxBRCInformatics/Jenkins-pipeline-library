#!/usr/bin/env groovy

import hudson.tasks.test.AbstractTestResultAction

def notifyBuild(String owningProject, boolean sendToNhsd) {
    // build status of null means successful
    String buildStatus = currentBuild.result ?: 'SUCCESS'

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
    def message = "${owningProject} [${baseName}] - #${env.BUILD_NUMBER} ${statusString} (<${env.BUILD_URL}|Open>)\n" +
                  "Time: ${timeString}\n" +
                  "${getTestResults()}"

    // Send notifications
    slackSend(color: colour, message: message)
    if(sendToNhsd)
        slackSend(color: colour, message: message,
                  channel: '#development', teamDomain: 'nhsdigitalssdgenomics', token: 'X1DrnvdZfv5ZF4qeuE9Gj5TN')
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