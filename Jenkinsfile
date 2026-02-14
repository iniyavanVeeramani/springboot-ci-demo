pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/iniyavanVeeramani/springboot-ci-demo.git'
            }
        }

       stage('Unit Tests') {
    steps {
        sh 'chmod +x mvnw'
        sh './mvnw clean test'
    }
}

       stage('Integration Tests & Package') {
    steps {
        sh 'chmod +x mvnw'
        sh './mvnw verify'
    }
}


        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            junit 'target/failsafe-reports/*.xml'
        }
    }
}
