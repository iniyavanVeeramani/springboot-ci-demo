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
    when {
        branch 'main'
    }
    steps {
        sh '''
        echo "Checking for currently running production container..."

        PREVIOUS_IMAGE=""

        if docker ps -a --format '{{.Names}}' | grep -q demo-prod; then
            PREVIOUS_IMAGE=$(docker inspect --format='{{.Config.Image}}' demo-prod)
            echo "Previous image found: $PREVIOUS_IMAGE"
        else
            echo "No previous production container found."
        fi

        echo "Stopping old container..."
        docker rm -f demo-prod || true

        echo "Starting new container with build ${BUILD_NUMBER}..."
        docker run -d --name demo-prod --network jenkins-network demo-app:${BUILD_NUMBER}

        echo "Waiting for application to become healthy..."

        i=1
        SUCCESS=0
        while [ $i -le 20 ]
        do
            if docker exec demo-prod wget -qO- http://localhost:8081/hello > /dev/null 2>&1
            then
                echo "New version is healthy!"
                SUCCESS=1
                break
            fi

            echo "Still starting... retry $i"
            sleep 5
            i=$((i+1))
        done

        if [ $SUCCESS -eq 1 ]; then
            echo "Deployment successful."
            exit 0
        fi

        echo "New deployment failed!"

        if [ ! -z "$PREVIOUS_IMAGE" ]; then
            echo "Rolling back to previous image: $PREVIOUS_IMAGE"
            docker rm -f demo-prod || true
            docker run -d --name demo-prod --network jenkins-network $PREVIOUS_IMAGE
            echo "Rollback completed."
        else
            echo "No previous image available for rollback."
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
