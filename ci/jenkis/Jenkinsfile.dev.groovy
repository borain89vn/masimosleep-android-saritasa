import groovy.json.JsonSlurper

def config = [
  vault_url: 'https://vault.saritasa.io/v1/project/data/masimosleep-android-develop',
  firebaseId: "1:272253104842:android:a619de24f7e5106f33fa33",
  firebaseGroup: "Masimo"
]

node("android"){
    try {
        JAVA_HOME = tool(name: 'jdk-latest', type: 'jdk')

        stage('Clean'){
            if ("${BUILD_CLEAN}" == "false") {
                cleanWs()
            }
        }


        stage('SCM'){
            checkout(scm)
        }
        
        stage('Credentials') {
            credentials = new JsonSlurper().parseText(
            httpRequest(
                customHeaders: [[maskValue: true, name: 'X-Vault-Token', value: "${env.VAULT_TOKEN}"]],
                url: config.vault_url
            ).content
            ).data

            fileContent = readFile('app/keystore/keystore.properties')
            for (item in credentials) {
            fileContent = fileContent.replace("%${item.key}%", "${item.value}")
            }
            writeFile(file: 'app/keystore/keystore.properties', text: fileContent)
        }

        stage('Build:apk') {
            withEnv(["JAVA_HOME=${JAVA_HOME}"]) {
                ansiColor('xterm') {
                    sh('./gradlew assembleEmulator')
               }
        
            }
        }
        stage('Deploy:firebase') {
                 sh("firebase appdistribution:distribute \
               app/build/outputs/apk/emulator/MasimoSleep*.apk \
                --app ${config.firebaseID} \
                --groups ${config.firebaseGroup}")
        }
        
        stage('Deploy:artifacts') {
            archiveArtifacts("app/build/outputs/apk/emulator/MasimoSleep*.apk")
        }
        
  } catch(error) {
    currentBuild.result = 'FAILURE'
    println("ERROR: ${error}")
  }

}