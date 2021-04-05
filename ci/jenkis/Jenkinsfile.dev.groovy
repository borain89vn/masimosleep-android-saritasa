
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

        stage('Build:apk') {
            withEnv(["JAVA_HOME=${JAVA_HOME}"]) {
                ansiColor('xterm') {
                    sh('./gradlew assembleEmulator')
               }
        
            }
        }

        stage('Deploy:artifacts') {
            archiveArtifacts("app/build/outputs/apk/emulation/MasimoSleep*.apk")
        }
        
  } catch(error) {
    currentBuild.result = 'FAILURE'
    println("ERROR: ${error}")
  }

}