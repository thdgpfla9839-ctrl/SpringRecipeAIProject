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
        APP_DIR = "~/app"
        JAR_NAME = "SpringRecipeAIProject-0.0.1-SNAPSHOT.jar"
        DOCKER_IMAGE = "thdgpfla5659/ai-app:latest"
    }
    // 우분투나 aws에서 명령어 수행
    stages {
        // 1. git checkout => 리포짓토리명 확인
        stage("Repository Checkout"){
            steps {
                // 2. 실행파일이 들어가는 위치
                echo 'Git Checkout'
                // scm 안에는 git-url, jenkinsfile 인식등이 저장돼 있음
                checkout scm
            }
        }
        // 3. yml 인식 => ${POST_URL}, api-key : ${GEN_KEY} 이거 인식시키려고
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
                    )
                ]){
                    sh '''
                       echo "SPRING_PROFILES_ACTIVE=prod" > .env
                       echo "POST_URL=${POST_URL}" >> .env
                       echo "GEN_KEY=${GEN_KEY}" >> .env
                        
                       chmod 600 .env
                       '''
                }
            }
        }
        // 4. gradlew 실행권한
        stage('Gradlew Permission'){
            steps {
                sh '''
                   chmod +x gradlew
                   '''
            }
        }
        
        // 5. gradlew build 시작 => 배포파일 만들기
        stage('Gradlew Build'){
            steps {
                sh '''
                     ./gradlew clean build -x test
                   '''
            }
        }
        // 6. Docker Image => 얘가 넘어갈 때마다 시간 측정이 됨
        // 도커 이미지 만드는 과정까지가 CI
        stage('Docker Build'){
            steps {
                sh '''
                   docker build -t ${DOCKER_IMAGE} . 
                   '''
            }
        }
        // 7. 도커허브에 전송 => 도커 로그인 먼저 해야함
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
                       echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                       '''
                }
            }
        }
        // 8. 도커 허브에 push
        stage('DockerHub Push'){
            steps {
                sh '''
                   docker push ${DOCKER_IMAGE}
                   '''
            }
        }
        
        // 9. 기존의 컨테이너(ai-app) 종료
        stage('DOCKER Compose DOWN'){
            steps {
                sh '''
                    docker compose down || true
                   '''
            }
        }
        
        // 10. 최신 이미지 읽어오기
        stage("DOCKER Compose Pull") {
            steps {
                sh '''
                    docker compose pull 
                   '''
            }
        }
        
        // 11. docker compose 실행
        stage("DOCKER Compose Up") {
            steps {
                sh '''
                    docker compose up -d 
                   '''
            }
        }
        
        // 12. 컨테이너 체크 
        stage("Container Check") {
            steps {
                sh '''
                    docker compose ps 
                   '''
            }
        }
    } // stages 종료

    post {
        success{
            echo '==============================================='
            echo 'Docker Compose 배포 성공'
            echo '==============================================='
        }
        failure {
            echo '==============================================='
            echo 'Docker Compose 배포 실패'
            echo '==============================================='
            sh '''
                docker compose ps || true
               '''
        }
    }
} // pipeline 종료