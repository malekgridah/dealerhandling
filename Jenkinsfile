pipeline {
    agent any
    tools {
        maven 'Maven 3' // This should match the name you configured in Global Tool Configuration
    }

    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
    }
}
