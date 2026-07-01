pipeline {
  agent any

  environment {
    MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-21'
    SONAR_HOST_URL = credentials('sonar-host-url')
    SONAR_TOKEN = credentials('sonar-token')
    MAVEN_RUN_ARGS = '-v maven_repository:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_RYUK_DISABLED=true -e TESTCONTAINERS_CHECKS_DISABLE=true -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal'
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

    stage('Quality Gate') {
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          sh '''
            set -e
            report="target/sonar/report-task.txt"
            if [ ! -f "$report" ]; then
              echo "Quality Gate: $report not found (did the SonarQube analysis run?)"
              exit 1
            fi
            ceTaskId=$(sed -n 's/^ceTaskId=//p' "$report")
            echo "Quality Gate: waiting for SonarQube task $ceTaskId ..."

            analysisId=""
            for _ in $(seq 1 60); do
              task=$(curl -sS -u "${SONAR_TOKEN}:" "${SONAR_HOST_URL}/api/ce/task?id=${ceTaskId}")
              status=$(echo "$task" | grep -o '"status":"[A-Z]*"' | head -1 | cut -d'"' -f4)
              echo "  analysis task status: ${status:-unknown}"
              case "$status" in
                SUCCESS)
                  analysisId=$(echo "$task" | grep -o '"analysisId":"[^"]*"' | head -1 | cut -d'"' -f4)
                  break ;;
                FAILED|CANCELED)
                  echo "Quality Gate: analysis task ${status}"; exit 1 ;;
                *)
                  sleep 5 ;;
              esac
            done

            if [ -z "$analysisId" ]; then
              echo "Quality Gate: timed out waiting for the analysis to finish"; exit 1
            fi

            gate=$(curl -sS -u "${SONAR_TOKEN}:" "${SONAR_HOST_URL}/api/qualitygates/project_status?analysisId=${analysisId}")
            qg=$(echo "$gate" | grep -o '"status":"[A-Z]*"' | head -1 | cut -d'"' -f4)
            echo "Quality Gate status: ${qg:-unknown}"
            if [ "$qg" != "OK" ]; then
              echo "$gate"
              echo "Quality Gate FAILED — failing the build."
              exit 1
            fi
            echo "Quality Gate passed."
          '''
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
