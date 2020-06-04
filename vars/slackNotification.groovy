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

    def colour = 'good'
    switch (buildStatus) {
        case 'UNSTABLE':
            colour = 'warning'
            break
        case 'FAILURE':
            colour = 'danger'
    }
    
    def buildURL = env.BUILD_URL
    echo buildURL
    def urlJobName = jobName.replaceAll('/','%2F).replaceAll(' ','%20')
    echo urlJobName
    def newBuildURL = buildURL.replace("job/${urlJobName}", "blue/organizations/jenkins/${urlJobName}")
    newBuildURL = newBuildURL.replace("job/${env.BRANCH_NAME}", "detail/${env.BRANCH_NAME}")
    echo newBuildURL
    
    

    def statusString = buildStatus.toLowerCase().capitalize()
    def timeString = Utils.getTime(currentBuild.startTimeInMillis, System.currentTimeMillis())
    
    // Default values
    def message = "${jobName} [${baseName}] - #${env.BUILD_NUMBER} ${statusString} (<${newBuildURL}|Open>)\n" +
                  "Time: ${timeString}"

    if(buildStatus != 'FAILURE') message += "\n${utils.getTestResults(currentBuild)}"

    echo message
    // Send notifications
    slackSend(color: colour, message: message, baseUrl: baseUrl)
}
