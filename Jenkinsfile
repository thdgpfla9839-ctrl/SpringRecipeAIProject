pipeline {
    agent any
    environment {
        APP_DIR="~/app"
        JAR_NAME="SpringRecipeAIProject-0.0.1-SNAPSHOT.jar"
    }
    stages {
        // git push => commit을 날리면 main으로 올라감
        // webhook / pull
        // Jenkins (local) => EC2
        // build
        // docker build
        // docker push
        // docker pull
        // docker run
        
        // deploy.yml에서 
        // -name: => stage 부분
        //   run: => steps 부분
        
        // 1. Repository 속 소스 파일을 체크하는 중 => 깃의 url 주소가 필요함
        stage('Check Out') {
            steps {
                echo 'Git Checkout'
                checkout scm
            }
        }
        
        // 임시로 뭐 만든대 
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
                        cat > .env << EOF
                        SPRING_PROFILES_ACTIVE=prod
                        POST_URL=${POST_URL}
                        GEN_KEY=${GEN_KEY}
                        EOF
                          chmod 600 .env
                       '''
                }
            }
        }
        
        // 2. gradlew build 하기 전에 permission 처리 해준다
        stage('Gradlew Permission'){
            steps {
                sh '''
                     chmod +x gradlew
                    '''
            }
        }
        // 3. gradlew build 시작
        stage('Gradlew Build'){
            steps {
                sh '''
                    ./gradlew clean build -x test
                   '''
            }
        }
        // 4. docker build
        stage('Docker Build'){
            steps {
                sh '''
                   docker build -t thdgpfla5659/ai-app:latest .
                   '''
            }
        }
        // 5. docker hub 로그인
        stage('DockerHub Login'){
            steps {
                   withCredentials([usernamePassword(
                       credentialsId:'dockerhub_info',
                       usernameVariable:'DH_USER',
                       passwordVariable:'DH_PASS'
                   )]){
                      sh '''
                        echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin   
                         '''
                   }
                   
            }
        }
        
        stage('Docker Push'){
            steps {
                sh '''
                    docker push thdgpfla5659/ai-app:latest
                   '''
            }
        }
        stage('Container Stop'){
            steps {
                sh '''
                    docker stop ai-app || true
                   '''
            }
        }
        
        stage('Container Remove'){
            steps {
                sh '''
                    docker rm ai-app || true
                   '''
            }
        }
        
        stage('DockerHub Pull'){
            steps {
                sh '''
                    docker pull thdgpfla5659/ai-app:latest
                   '''
            }
        }
        
        stage('Docker Run'){
            steps {
                sh '''
                    docker run -d --name ai-app -p 9090:9090 --env-file .env thdgpfla5659/ai-app:latest
                   '''
            }
        }
    }
}