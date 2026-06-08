pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/diegoalonso1208-ux/rapidocourier_Trabajo_final_Diego_Ortega.git'
            }
        }

        stage('Build - eureka-server') {
            steps {
                dir('eureka-server') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build - servicio-auth') {
            steps {
                dir('servicio-auth') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build - servicio-clientes') {
            steps {
                dir('servicio-clientes') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build - servicio-paquetes') {
            steps {
                dir('servicio-paquetes') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build - servicio-tarifas') {
            steps {
                dir('servicio-tarifas') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test - servicio-auth') {
            steps {
                dir('servicio-auth') {
                    bat 'mvn test'
                }
            }
        }

        stage('Test - servicio-clientes') {
            steps {
                dir('servicio-clientes') {
                    bat 'mvn test'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline ejecutado exitosamente!'
        }
        failure {
            echo 'Pipeline falló!'
        }
    }
}