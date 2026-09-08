const {defineStore} = Pinia
/*
    [Pinia 구성도]
    
    state : 공통으로 사용되는 변수 => 이 변수가 변경(상태)이 될때마다 html에 적용하는 변수듷의 집합 => 자바에서는 static 변수
    getters : computed랑 똑같음 => 계산이나 이미 지정된 값에 주로 사용 => 페이지 출력, 수량 계산, 천자리 앞에 , 찍기
    actions : 실제 서버와 연결해서 state 변수를 변경해주는 역할을 수행
    
    => React 속 Redux랑 사용이 비슷하다
    => getters는 Tanstack-Query에서 등장
    => 이게 어렵다 보니 FrameWork화 해서 NextJs
    ===============================================================================================================
    
    참고로 백엔드: 자바, 오라클,mysql,postgreSQL이 기본
                jsp / 타임리프
                SpringFrameWork/SpringBoot
                Security => 일반 springSecurity / JWT
                WebSocket => STOMP/SOCKJS
                =====================================================여기까찌가 기본 밑에는 우대사항
                SpringAI => EmbeddingModel / ChatClientModel
                         => RAG
                         => MCP 
                Kafka
                
        프론트 : JQuery 4.0 / AJAX
               Vue(뷰에선 pinia) / React(여기에서는 tanstack-query => 더 나아가서는 NextJS)
               ====================================================================
               우대사항 
               typeScript / NodeJS
               
        SE(시스템엔지니어 => CI/CD) : GIT ACTION => workflows
                                  DOCKER
                                  DOCKER COMPOSE
                                  AWS => EC2 / S3 / RDS                          
 */

/*
  defineStore : 새로운 store를 만들 때 사용하는 변수
*/

const useRecipeStore=defineStore('recipe',
    // state => 예전에 data(){return {}} 이 안에 들어가던 변수가 state로 바뀐 거
    ()=>{
        // 선택된 재료 배열
       const selectedIngredients = ref([])
       // 현재 선택된 카테고리
       const selectedCategory=ref("all")
       // AI 검색 로딩 여부
       const loading=ref(false)
       // 오류 메시지 (오류 처리)
       const errMessage=ref('')
       // 화면에 표시할 재료의 종류
       const ingredients = [

           {
               name:"돼지고기",
               icon:"🥩",
               category:"육류"
           },

           {
               name:"소고기",
               icon:"🥩",
               category:"육류"
           },

           {
               name:"닭고기",
               icon:"🍗",
               category:"육류"
           },

           {
               name:"김치",
               icon:"🥬",
               category:"채소"
           },

           {
               name:"대파",
               icon:"🌱",
               category:"채소"
           },

           {
               name:"양파",
               icon:"🧅",
               category:"채소"
           },

           {
               name:"감자",
               icon:"🥔",
               category:"채소"
           },

           {
               name:"당근",
               icon:"🥕",
               category:"채소"
           },

           {
               name:"마늘",
               icon:"🧄",
               category:"채소"
           },

           {
               name:"계란",
               icon:"🥚",
               category:"계란/유제품"
           },

           {
               name:"두부",
               icon:"🧊",
               category:"계란/유제품"
           },

           {
               name:"우유",
               icon:"🥛",
               category:"계란/유제품"
           },

           {
               name:"치즈",
               icon:"🧀",
               category:"계란/유제품"
           },

           {
               name:"쌀",
               icon:"🍚",
               category:"곡류"
           },

           {
               name:"밀가루",
               icon:"🌾",
               category:"곡류"
           },

           {
               name:"라면",
               icon:"🍜",
               category:"곡류"
           },

           {
               name:"고춧가루",
               icon:"🌶️",
               category:"양념"
           },

           {
               name:"고추장",
               icon:"🫙",
               category:"양념"
           },

           {
               name:"된장",
               icon:"🫙",
               category:"양념"
           },

           {
               name:"간장",
               icon:"🍶",
               category:"양념"
           },

           {
               name:"소금",
               icon:"🧂",
               category:"양념"
           },

           {
               name:"참치",
               icon:"🐟",
               category:"수산물"
           },

           {
               name:"고등어",
               icon:"🐟",
               category:"수산물"
           },

           {
               name:"새우",
               icon:"🦐",
               category:"수산물"
           }

       ]
       const searchKeyword=ref('') // v-model이 연결되는 곳
       
       const filteredIngredients=computed(()=>{
          const keyword=searchKeyword.value
                             .trim()
                             .toLowerCase()
                        return ingredients.value.filter(
                            ingredient =>{
                                // 카테고리 조건 검사
                                const categoryMatch=
                                selectedCategory.value === 'all'
                                ||
                                ingredient.category === selectedCategory.value
                                // 검색어 조건 검사
                                const searchMatch = ingredient.name
                                .toLowerCase()
                                .includes(keyword)
                                // 두 조건이 true일 경우
                                return categoryMatch && searchMatch
                            }
                        )
       })
       function toggleIngredient(name){
        // 현재 선택이 돼있는지 확인하기 위함 
        const index=selectedIngredients.value.indexOf(name)
         // 이미 선택이 됐는지
         if(index!==-1)
            {
                // 배열에서 삭제
                selectedIngredients.value.splice(index,1)
            }
          else
              {
                selectedIngredients.value.push(name)     
              }
       }
       // 선택여부 확인
       
    }
    // getters
    // actions
)