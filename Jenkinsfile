/*

    전체 동작에서 jenkins는 관리자 역할
      Git Push => git의 workflows
               => WebHook(여기서 젠킨스로 넘어감 -> 트리거)
          |
        Jenkins
          | => permission 방지 (chmod +x gradlew) => 실행 권한 부여
     Gradle Build          
          |
      ./gradlew clean build -x test  => test를 제외하고 jar파일을 만들어라
          |
     Docker Build => image를 만든다 => docker build -t image
          |
     Docker hub Push => docker push image명칭
          |
         서버 종료 
     Docker Compose down
           |
     Docker compose pull        
           |
     Docker compose up -d      
*/

pipeline {
    agent any
    // 변수 설정 => environment
    environment {
        
        JAR_NAME = "SpringRecipeAIProject-0.0.1-SNAPSHOT.jar"
        DOCKER_IMAGE = "thdgpfla5659/ai-app:latest"
        
        // AWS EC2와 관련
        SERVER_USER="ubuntu"
        // 우분투 아이피
        SERVER_IP="43.200.129.248"
        APP_DIR="/home/ubuntu/app"
    }
    // 우분투나 aws에서 명령어 수행
    stages {
        // 1. git checkout => 리포짓토리명 확인
        stage("Repository Checkout"){
            steps {
                echo 'Git Checkout'
                checkout scm
            }
        }
        //3. Java JDK 확인
        stage("JDK21 확인"){
            steps {
                sh '''
                    java -version
                   '''
            }
        }
        // 5. gradlew 실행권한
        stage('Gradlew Permission'){
            steps {
                sh '''
                   chmod +x gradlew
                   '''
            }
        }
        
        // 6. gradlew build 시작 => 배포파일 만들기
        stage('Gradlew Build'){
            steps {
                sh '''
                     ./gradlew clean build -x test
                   '''
            }
        }
        // 7. Docker Image
        stage('Docker Build'){
            steps {
                sh '''
                   docker build -t ${DOCKER_IMAGE} . 
                   '''
            }
        }
        // 8. 도커허브에 전송
        stage('DockerHub Login'){
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId:'dockerhub_info',
                        usernameVariable:'DH_USER',
                        passwordVariable:'DH_PASS'
                    )
                ]){
                    sh '''
                       echo "$DH_PASS"  docker login -u "$DH_USER" --password-stdin
                       '''
                }
            }
        }
        // 9. 도커 허브에 push
        stage('DockerHub Push'){
            steps {
                sh '''
                   docker push ${DOCKER_IMAGE}
                   '''
            }
        }
        // 10. ssh key 설정
        stage("SSH Key Setting"){
            steps {
                withCredentials([
                    sshUserPrivateKey(
                       credentialsId: 'SERVER_SSH_KEY',
                       keyFileVariable: 'SSH_KEY',
                       usernameVariable: 'SSH_USER'
                    )
                ]){
                    sh '''
                        mkdir -p ~/.ssh
                        cp "$SSH_KEY" ~/.ssh/id_ed25519
                        chmod 600 ~/.ssh/id_ed25519
                       '''
                }
            }
        }
        
        // 11. aws 접근
        stage("Known Hosts"){
            steps {
                sh '''
                    mkdir -p ~/.ssh
                    ssh-keyscan -H 43.200.129.248 >> ~/.ssh/Known_hosts
                    
                    chmod 644 ~/.ssh/Known_hosts
                   '''
            }
        }
        // 12. .env 생성 (EOF 제거 → && 연결 방식으로 변경)
        stage('Create .env'){
            steps {
                withCredentials([
                    string(
                        credentialsId: 'post-url',
                        variable: 'POST_URL'
                    ),
                    string(
                        credentialsId: 'gen-key',
                        variable: 'GEN_KEY'
                    ),
                    sshUserPrivateKey(
                       credentialsId: 'SERVER_SSH_KEY',
                       keyFileVariable: 'SSH_KEY',
                       usernameVariable: 'SSH_USER'
                    )
                ]){
                    sh """
                       ssh -i \$SSH_KEY -o StrictHostKeyChecking=no ubuntu@43.200.129.248 \
                         "mkdir -p /home/ubuntu/app && \
                          cd /home/ubuntu/app && \
                          rm -f .env && \
                          echo 'SPRING_PROFILES_ACTIVE=prod' > .env && \
                          echo 'POST_URL=${POST_URL}' >> .env && \
                          echo 'GEN_KEY=${GEN_KEY}' >> .env && \
                          chmod 600 .env"
                       """
                }
            }
        }
        // 13. docker-compose.yml 이동
        stage("Copy Docker-Compose"){
            steps {
                 withCredentials([
                    sshUserPrivateKey(
                       credentialsId: 'SERVER_SSH_KEY',
                       keyFileVariable: 'SSH_KEY',
                       usernameVariable: 'SSH_USER'
                    )
                ]){
                    sh '''
                       ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no ubuntu@43.200.129.248 "mkdir -p /home/ubuntu/app"
                       
                       scp -i "SSH_KEY" -o StrictHostKeyChecking=no ubuntu@43.200.129.248 docker-compose.yml ubuntu@43.200.129.248:/home/ubuntu/app/docker-compose.yml
                       '''
                }
            }
        }
        // 14. Deploy (EOF 제거 → && 연결 방식으로 변경)
        stage("Deploy"){
            steps {
                 withCredentials([
                    sshUserPrivateKey(
                       credentialsId: 'SERVER_SSH_KEY',
                       keyFileVariable: 'SSH_KEY',
                       usernameVariable: 'SSH_USER'
                    )
                ]){
                    sh """
                        ssh -i \$SSH_KEY -o StrictHostKeyChecking=no ubuntu@43.200.129.248 \
                          "cd /home/ubuntu/app && \
                           docker-compose down && \
                           docker-compose pull && \
                           docker-compose up -d"
                       """
                }
            }
        }
    }
} // pipeline 종료