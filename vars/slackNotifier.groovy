#!/usr/bin/env groovy
import uk.ac.ox.ndm.jenkins.Utils

def call() {
    String buildStatus = currentBuild.currentResult

    def utils = new Utils()
    if(utils.hasFailedTests(currentBuild)) buildStatus = 'UNSTABLE'

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


    def statusString = buildStatus.toLowerCase().capitalize()
    def timeString = Utils.getTime(currentBuild.startTimeInMillis, System.currentTimeMillis())


    // Default values
    def message = "${jobName} [${baseName}] - #${env.BUILD_NUMBER} ${statusString} (<${env.BUILD_URL}|Open>)\n" +
                  "Time: ${timeString}\n" +
                  "${utils.getTestResults(currentBuild)}"

    echo message
    // Send notifications
    slackSend(color: colour, message: message)
}


