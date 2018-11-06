import hudson.*
import hudson.model.*
import jenkins.*
import jenkins.model.*

def call(Integer MAX_BUILDS) {
    //echo new Date(System.currentTimeMillis()).format('MM/dd/yyyy hh:mm:ss a') + " / " + " -- Start Time"

    for (job in Jenkins.instance.items) {

        int count = 0

        echo "\n ***Job Name: " + job.name + "***"

        if (job.workspace != null && job.workspace != "") //Check if there is a workspace associated with the Job
        {
            echo "Workspace path : " + job.workspace

            String workspace = job.workspace

            File folder = new File(workspace)

            if (folder != null && folder.exists()) //Check if the Workspace folder exists
            {
                // Get all files and folders within the Workspace of current job.
                //Iterate through only folders and sort em by Modified Date.

                File[] files = new File(workspace).listFiles().sort() {
                    a, b -> b.lastModified().compareTo a.lastModified()
                }
                .each {
                    if (!it.isFile()) //Check only for folders
                    {
                        if (count < MAX_BUILDS)
                            echo new Date(it.lastModified()).format('MM/dd/yyyy hh:mm:ss a') + " /" + it.name + " -- Save"
                        else {
                            echo new Date(it.lastModified()).format('MM/dd/yyyy hh:mm:ss a') + " /" + it.name +
                                                            " ** Deleted"
                           // it.deleteDir()
                        }
                        count++
                    }
                }
            } else {
                echo "Workspace is empty or doesn't exist"
            }
        } else {
            echo "No Workspace associated with this job"
        }
    }

    //echo new Date(System.currentTimeMillis()).format('MM/dd/yyyy hh:mm:ss a') + " / " + " -- End Time"
}