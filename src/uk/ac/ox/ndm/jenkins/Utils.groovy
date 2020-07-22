package uk.ac.ox.ndm.jenkins

import hudson.tasks.test.AbstractTestResultAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @since 22/02/2017
 */
class Utils implements Serializable {

    String getTestResults(currentBuild) {
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
        results
    }

    boolean hasFailedTests(currentBuild){
        AbstractTestResultAction testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
        testResultAction != null ? testResultAction.failCount : false
    }

    static String getTime(long start, long end) {

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

    static int findFreeTcpPort(){
        int port
        new ServerSocket(0).withCloseable {
            port = it.getLocalPort()
        }
        port
    }

    static String generateRandomTestFolder(){
        String folder = "/tmp/${UUID.randomUUID().toString()}"
        Path path = Paths.get(folder)
        Files.createDirectories(path)
        folder
    }

    static String findProperty(String ws, String filename,String propertyName){
        Path p = Paths.get(ws).resolve(filename)
        if(Files.exists(p)){
            List<String> lines = p.readLines()
            for (int i = 0; i < lines.size(); i++) {
                if(lines[i].startsWith(propertyName)){
                    return lines[i].split('=')[1]
                }
            }
        }
        null
    }
}
