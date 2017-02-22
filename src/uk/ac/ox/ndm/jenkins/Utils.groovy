package uk.ac.ox.ndm.jenkins

import hudson.tasks.test.AbstractTestResultAction

/**
 * @since 22/02/2017
 */
class Utils {

    def steps

    Utils(steps){
        this.steps = steps
    }

    String getTestResults() {
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

        steps.echo "${results}"

        results
    }


    String getTime(long start, long end) {

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
}
