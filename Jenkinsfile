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


stage('Deploy') {
    when {
        anyOf {
            branch 'main'
            branch 'develop'
        }
    }
    steps {
        script {

            def containerName = ""
            def portMapping = ""

            if (env.BRANCH_NAME == "main") {
                containerName = "demo-prod"
                portMapping = "-p 8091:8081"
                echo "Deploying to PRODUCTION"
            }

            if (env.BRANCH_NAME == "develop") {
                containerName = "demo-staging"
                portMapping = "-p 8092:8081"
                echo "Deploying to STAGING"
            }

            sh """
            docker rm -f ${containerName} || true

            docker run -d --name ${containerName} \
            --network jenkins-network \
            ${portMapping} \
            demo-app:${BUILD_NUMBER}

            echo "Waiting for application..."

            i=1
            while [ \$i -le 20 ]
            do
                if docker exec ${containerName} wget -qO- http://localhost:8081/hello > /dev/null 2>&1
                then
                    echo "Deployment successful for ${containerName}"
                    exit 0
                fi

                echo "Retry \$i..."
                sleep 5
                i=\$((i+1))
            done

            echo "Deployment failed for ${containerName}"
            docker logs ${containerName}
            exit 1
            """
        }
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
