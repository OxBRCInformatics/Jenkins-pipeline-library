#!/usr/bin/env groovy
import uk.ac.ox.ndm.jenkins.Utils

def call(String baseUrl = 'https://oxfordbrcinformatics.slack.com/services/hooks/jenkins-ci/') {
    String buildStatus = currentBuild.currentResult
    def utils = new Utils()
    if (!(buildStatus in ['FAILURE','UNSTABLE'])) {
        if (utils.hasFailedTests(currentBuild)) buildStatus = 'UNSTABLE'
    }

    String baseName = env.JOB_BASE_NAME
    baseName = baseName.replaceAll(/%2F/, '/')

    def jobName = env.JOB_NAME.substring(0, env.JOB_NAME.lastIndexOf('/'))
echo jobName
    def colour = 'good'
    switch (buildStatus) {
        case 'UNSTABLE':
            colour = 'warning'
            break
        case 'FAILURE':
            colour = 'danger'
    }

    def statusString = buildStatus.toLowerCase().capitalize()
    def timeString = Utils.getTime(currentBuild.startTimeInMillis, System.currentTimeMillis())
    def buildURL = env.BUILD_URL
    echo buildURL
    def newBuildURL = buildURL.replace("job/${env.JOB_NAME}", "blue/organizations/jenkins/${env.JOB_NAME}")
    echo env.JOB_NAME
    echo env.BRANCH_NAME
    newBuildURL = newBuildURL.replace("job/${env.BRANCH_NAME}", "detail/${env.BRANCH_NAME}")
    echo newBuildURL
    // Default values
    def message = "${jobName} [${baseName}] - #${env.BUILD_NUMBER} ${statusString} (<${newBuildURL}|Open>)\n" +
                  "Time: ${timeString}"

    if(buildStatus != 'FAILURE') message += "\n${utils.getTestResults(currentBuild)}"

    echo message
    // Send notifications
    slackSend(color: colour, message: message, baseUrl: baseUrl)
}
