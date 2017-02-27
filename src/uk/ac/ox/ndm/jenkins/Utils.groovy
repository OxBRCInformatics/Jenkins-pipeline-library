package uk.ac.ox.ndm.jenkins

import hudson.tasks.test.AbstractTestResultAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

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

    static List<Path> getGrailsDirectoriesWithIntegrationTestFolders(String ws){
        Path workspace = Paths.get(ws)
        Files.walk(workspace).filter({path -> Files.exists(path.resolve('src/integration-test'))}).collect(Collectors.toList())
    }
}
