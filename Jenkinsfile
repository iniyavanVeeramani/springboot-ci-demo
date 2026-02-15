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

stage('Check Docker Access') {
            steps {
                sh 'docker --version'
            }
        }

stage('Build & Push Docker Image') {
    when {
        branch 'main'
    }
    steps {
        withCredentials([usernamePassword(credentialsId: 'docker-hub-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh '''
            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
            docker build -t demo-app:${BUILD_NUMBER} .
            docker tag demo-app:${BUILD_NUMBER} iniyavan128/demo-app:${BUILD_NUMBER}
            docker tag demo-app:${BUILD_NUMBER} iniyavan128/demo-app:latest
            docker push iniyavan128/demo-app:${BUILD_NUMBER}
            docker push iniyavan128/demo-app:latest
            '''
        }
    }
}



stage('Deploy Container') {
    steps {
        sh '''
        docker rm -f demo-container || true
        docker run -d -p 8090:8080 --name demo-container demo-app:${BUILD_NUMBER}

        echo "Waiting for application to start..."
        sleep 20

        echo "Checking health endpoint..."
        docker exec demo-container curl -f http://localhost:8080/hello
        '''
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
