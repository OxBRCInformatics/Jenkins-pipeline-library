/**
 * @since 07/07/2017
 */

String call(String fileName='gradle.properties'){

    String propertyContents = readFile(fileName)
    List<String> properties = propertyContents.readLines()

    for (int i = 0; i < properties.size(); i++) {
        if(properties[i]) {
            String[] keyValue = properties[i].split('=')
            if(keyValue.size() == 2) {
                String key = keyValue[0]
                String value = keyValue[1]
                if(key == 'version'){
                    return value.replace('-SNAPSHOT','')
                }
            }
        }
    }
    null
}