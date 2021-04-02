
node("android"){
    try {
        JAVA_HOME = tool(name: 'jdk-latest', type: 'jdk')

        stage('Clean'){
            if ("${BUILD_CLEAN}" == "true") {
                cleanWs()
            }
        }
        stage('SCM'){
            checkout(scm)
        }

        stage('build') {
            withEnv(["JAVA_HOME=${JAVA_HOME}"]) {
                sh('./gradlew assembleDebug')
        
            }
        }
        stage('Artifacts') {
            archiveArtifacts("app/build/outputs/apk/debug/MasimoSleep*.apk")
        }
  } catch(error) {
    currentBuild.result = 'FAILURE'
    println("ERROR: ${error}")
  }

}