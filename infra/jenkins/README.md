# Jenkins

The local Jenkins image includes Docker CLI and Docker Compose plugin support so the pipeline can build service images.

Required Jenkins credentials:

- `sonar-host-url`: secret text, for example `http://sonarqube:9000`.
- `sonar-token`: secret text generated from SonarQube.

The pipeline is defined in the repository root `Jenkinsfile`.
