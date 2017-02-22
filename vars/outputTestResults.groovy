import uk.ac.ox.ndm.jenkins.Utils

/**
 * @since 22/02/2017
 */

def call(){
    Utils utils = new Utils()
    echo utils.getTestResults(currentBuild)
}