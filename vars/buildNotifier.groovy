#!/usr/bin/env groovy
import uk.ac.ox.ndm.jenkins.Utils

def call(String owningProject = 'Jenkins', boolean sendToNhsd = true) {
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

    def utils = new Utils(steps)
    def statusString = buildStatus.toLowerCase().capitalize()
    def timeString = utils.getTime(currentBuild.startTimeInMillis, System.currentTimeMillis())

    // Default values
    def message = "${owningProject} [${baseName}] - #${env.BUILD_NUMBER} ${statusString} (<${env.BUILD_URL}|Open>)\n" +
                  "Time: ${timeString}\n" +
                  "${utils.getTestResults()}"

    // Send notifications
    slackSend(color: colour, message: message)
    if (sendToNhsd)
        slackSend(color: colour, message: message,
                  channel: '#development', teamDomain: 'nhsdigitalssdgenomics', token: 'X1DrnvdZfv5ZF4qeuE9Gj5TN')
}


