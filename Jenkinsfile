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
        echo "Starting deployment of version ${BUILD_NUMBER}..."

        PREVIOUS_IMAGE=$(docker ps -a --format '{{.Image}}' | grep demo-app | head -n 1 || true)

        docker rm -f demo-container || true

        docker run -d --name demo-container --network jenkins-network demo-app:${BUILD_NUMBER}

        echo "Waiting for application to become healthy..."

        i=1
        while [ $i -le 20 ]
        do
            if docker exec demo-container wget -qO- http://localhost:8081/hello > /dev/null 2>&1
            then
                echo "Application is UP!"
                echo "Running version:"
                docker inspect demo-container --format='{{.Config.Image}}'
                exit 0
            fi

            echo "Still starting... retry $i"
            sleep 5
            i=$((i+1))
        done

        echo "Deployment failed. Showing logs:"
        docker logs demo-container

        if [ ! -z "$PREVIOUS_IMAGE" ]; then
            echo "Rolling back to previous image: $PREVIOUS_IMAGE"
            docker rm -f demo-container || true
            docker run -d --name demo-container --network jenkins-network $PREVIOUS_IMAGE
            echo "Rollback completed."
        else
            echo "No previous image found for rollback."
        fi

        exit 1
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
