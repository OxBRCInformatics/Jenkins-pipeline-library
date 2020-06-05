#!/usr/bin/env groovy
import uk.ac.ox.ndm.jenkins.Utils

def call(String baseUrl = 'https://oxfordbrcinformatics.slack.com/services/hooks/jenkins-ci/') {
    String buildStatus = currentBuild.currentResult
    Utils utils = new Utils()
    if (!(buildStatus in ['FAILURE', 'UNSTABLE'])) {
        if (utils.hasFailedTests(currentBuild)) buildStatus = 'UNSTABLE'
    }

    String colour = 'good'
    switch (buildStatus) {
        case 'UNSTABLE':
            colour = 'warning'
            break
        case 'FAILURE':
            colour = 'danger'
    }

    String envJobName = env.JOB_NAME
    String envBuildUrl = env.BUILD_URL
    String envBranchName = env.BRANCH_NAME

    String repoName = envJobName.substring(0, envJobName.lastIndexOf('/'))
    String oldUrlBranchName = envBranchName.replaceAll('%2F','%252F')

    String newBuildURL
    int lastIndex = repoName.lastIndexOf('/')

    // Replace the repoName with the blue start
    if (lastIndex != -1) {
        String urlRepoName = repoName.replaceAll('/', '%2F').replaceAll(' ', '%20')

        String orgName = repoName.substring(0, lastIndex).replaceAll(' ', '%20')
        String orgRepoName = repoName.substring(lastIndex+1)

        newBuildURL = envBuildUrl.replace("job/${orgName}/job/${orgRepoName}", "blue/organizations/jenkins/${urlRepoName}")
    } else {
        newBuildURL = envBuildUrl.replace("job/${repoName}", "blue/organizations/jenkins/${repoName}")
    }

    // Replace the branch name with the blue detail
    newBuildURL = newBuildURL.replace("job/${oldUrlBranchName}", "detail/${env.JOB_BASE_NAME}")

    String statusString = buildStatus.toLowerCase().capitalize()
    String timeString = Utils.getTime(currentBuild.startTimeInMillis, System.currentTimeMillis())

    // Default values
    String message = "${repoName} [${env.BRANCH_NAME}] - #${env.BUILD_NUMBER} ${statusString} (<${newBuildURL}|Open>)\n" +
                  "Time: ${timeString}"

    if (buildStatus != 'FAILURE') message += "\n${utils.getTestResults(currentBuild)}"

    echo message
    // Send notifications
    slackSend(color: colour, message: message, baseUrl: baseUrl)
}

// env.BRANCH_NAME = feature/upgrade-to-grails-4
// env.JOB_BASE_NAME = feature%2Fupgrade-to-grails-4
// env.JOB_NAME = mc-core/feature%2Fupgrade-to-grails-4
// env.BUILD_URL = https://jenkins.cs.ox.ac.uk/job/mc-core/job/feature%252Fupgrade-to-grails-4/127/
//
// https://jenkins.cs.ox.ac.uk/blue/organizations/jenkins/mc-core/detail/feature%2Fupgrade-to-grails-4/127/