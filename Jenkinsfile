pipeline {
  agent any

  environment {
    MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-21'
    SONAR_HOST_URL = credentials('sonar-host-url')
    SONAR_TOKEN = credentials('sonar-token')
    MAVEN_RUN_ARGS = '-v maven_repository:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_RYUK_DISABLED=true -e TESTCONTAINERS_CHECKS_DISABLE=true'
  }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Unit Tests') {
      steps {
        script {
          docker.image(env.MAVEN_IMAGE).inside(env.MAVEN_RUN_ARGS) {
            sh 'mvn -B clean test'
          }
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        script {
          docker.image(env.MAVEN_IMAGE).inside(env.MAVEN_RUN_ARGS) {
            sh '''
              mvn -B verify sonar:sonar \
                -Dsonar.host.url="${SONAR_HOST_URL}" \
                -Dsonar.token="${SONAR_TOKEN}"
            '''
          }
        }
      }
    }

    stage('Build Docker Images') {
      steps {
        sh 'docker compose -f infra/docker/docker-compose.yml --env-file .env.example build'
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
    }
  }
}
