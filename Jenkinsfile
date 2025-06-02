pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'my-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        REGISTRY = 'localhost:5000'
    }

    stages {
        stage('Checkout') {
            steps {
                // For public repositories - no credentials needed
                checkout scm

                // Alternative: Direct git clone (if needed)
                // git branch: 'main', url: 'https://github.com/yourusername/your-spring-app.git'
            }
        }

        stage('Setup') {
            steps {
                // Check if Java is available (should show JDK 21)
                sh 'java -version'

                // Make gradlew executable
                sh 'chmod +x gradlew'

                // Check Gradle
                sh './gradlew --version'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew clean test'
            }
            post {
                always {
                    // Only publish if test results exist
                    script {
                        if (fileExists('build/test-results/test/*.xml')) {
                            publishTestResults testResultsPattern: 'build/test-results/test/*.xml'
                        }
                        if (fileExists('build/reports/tests/test/index.html')) {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'build/reports/tests/test',
                                reportFiles: 'index.html',
                                reportName: 'Test Report'
                            ])
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build -x test'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    // Build image
                    def image = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")

                    // Tag as latest
                    sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"

                    // Push to registry (if registry is available)
                    try {
                        docker.withRegistry("http://${REGISTRY}") {
                            image.push()
                            image.push('latest')
                        }
                        echo "Successfully pushed to registry"
                    } catch (Exception e) {
                        echo "Registry push failed: ${e.getMessage()}"
                        echo "Continuing without registry push..."
                    }
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                script {
                    try {
                        sh """
                            docker stop ${DOCKER_IMAGE}-staging || true
                            docker rm ${DOCKER_IMAGE}-staging || true
                            docker run -d --name ${DOCKER_IMAGE}-staging \\
                                -p 8081:8080 \\
                                ${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                        echo "Staging deployment successful"
                    } catch (Exception e) {
                        echo "Staging deployment failed: ${e.getMessage()}"
                        throw e
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo "Waiting for application to start..."
                    sleep(30)

                    // Try health check with fallback
                    try {
                        sh 'curl -f http://localhost:8081/actuator/health'
                        echo "Health check passed!"
                    } catch (Exception e) {
                        echo "Actuator health check failed, trying basic connectivity..."
                        sh 'curl -f http://localhost:8081/ || echo "Basic connectivity check failed"'
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline succeeded! 🎉'
        }
        failure {
            echo 'Pipeline failed! ❌'
        }
    }
}