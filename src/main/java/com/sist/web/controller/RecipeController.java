package com.sist.web.controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sist.web.service.*;


import lombok.RequiredArgsConstructor;

/**
 * ============================================================
 * RecipeController
 * ============================================================
 *
 * URL
 *
 * GET
 * /recipe/recommend
 *
 * POST
 * /recipe/recommend
 *
 * 처리 순서
 *
 * HTML
 *   ↓
 * 선택 재료
 *   ↓
 * AJAX POST
 *   ↓
 * Controller
 *   ↓
 * RecipeVectorService
 *   ↓
 * EmbeddingModel
 *   ↓
 * PostgreSQL pgVector
 *   ↓
 * JSON
 *   ↓
 * HTML 결과 출력
 * ============================================================
 */


/*
 *  [전체 동작 과정]
 *   <브라우저> : HTML / JavaScript(순수 자바스크립트인 바닐라js)
 *    |
 *    재료 선택
 *    |
 *    타임리프에서 전송(post  /recipe/recommand)
 *    |
 *    RecipeController가 값을 받음
 *    => 1) @Getmapping("/recipe/recommand") => 화면 ui 전송
 *       2) @postmapping("/recipe/recommand") => 데이터 전송 시 
 *       같은 경로가 들어가면 안 되지만 get방식과 post방식으로 할 땐 상관없다
 *       추가로 @ResponseBody => 문자열이나 JSON 전송핳 때 사용 => RestController로 변경
 *   |
 *   ingredients 전달(재료)
 *   |
 *   RecipeService가 받음
 *   => 1) 재료가 있는지 확인
 *      2) 검색하는 문장 생성
 *      3) EmbeddingModel 생성
 *      4) String으로 저장된 것들을  => float[]로 변경 : vector
 *      5) PostgreSQL + pgVector안에서 유사 검색 => 원래는 Like 문장으로 검색해봤지
 *      6) 레시피에서 content를 추출
 *      7) 냉장고에서 보내준 데이터와 레시피 재료를 비교
 *      8) 재료상태가 어떤 상태인지=> RecipeService를 보면 재료 가지고 있는지 여부
 *      9) 8을 갖고 재료 충족률을 계산
 *   |
 *   추천 레시피 List => Limit 5
 *   => 1) 보유하고 있는 재료
 *      2) 부족한 재료
 *      3) 재료가 몇프로 충족되는지 충족률
 *      4) 레시피명
 *      5) 조리방법
 *      6) 요리종류
 *      7) 조리 과정
 *  --------------------------------------------------------------------------
 *  |
 *  타임리프 화면
 *  => HTML - Controller - RecipeService - (EmbeddingModel - PostgreSQL + pgVector - 유사 레시피 찾기 - 재료 확인) 이 부분이 스프링AI로 처리한 부분
 *  |
 *  이제 HTML에서 출력   
 */ 

// 지금 자바스크립트로 돼 있는데 이 부분을 pinia로 수정해야하고
//  @ResponseBody를 @restController로
//  @Tool => Tool Calling => 프롬프트 검색
//  기능별 분리 => MCP
@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {


    /*
     * Vector 검색 Service
     */
    private final RecipeService recipeVectorService;


    /**
     * ========================================================
     * 레시피 추천 화면
     * ========================================================
     *
     * GET
     *
     * http://localhost:8080/recipe/recommend
     */
    @GetMapping("/recommand")
    public String recommendPage(Model model) {

        /*
         * 처음에는 검색 결과가 없도록 설정
         */
        model.addAttribute(
                "recipes",
                Collections.emptyList()
        );

        return "recipe/recommand";
    }


    /**
     * ========================================================
     * 레시피 Vector 검색
     * ========================================================
     *
     * POST
     *
     * /recipe/recommend
     *
     * JSON
     *
     * {
     *   "ingredients": [
     *      "김치",
     *      "돼지고기",
     *      "두부"
     *   ]
     * }
     */
    @PostMapping("/recommand")
    @ResponseBody
    public Map<String, Object> recommand(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response =
                new HashMap<>();


        try {

            /*
             * JSON에서 ingredients 추출
             */
            Object ingredientObject =
                    request.get("ingredients");

            /*
             * 재료가 없는 경우
             */
            if (ingredientObject == null) {

                response.put(
                        "success",
                        false
                );

                response.put(
                        "message",
                        "재료를 선택해주세요."
                );

                response.put(
                        "recipes",
                        Collections.emptyList()
                );

                return response;
            }


            /*
             * JSON 배열 → List<String>
             */
            List<String> ingredients =
                    new ArrayList<>();

            if (ingredientObject instanceof List<?>) {

                List<?> list =
                        (List<?>) ingredientObject;

                for (Object value : list) {

                    if (value != null) {

                        String ingredient =
                                value.toString().trim();

                        if (!ingredient.isEmpty()) {

                            ingredients.add(
                                    ingredient
                            );
                        }
                    }
                }
            }


            /*
             * 선택 재료가 없는 경우
             */
            if (ingredients.isEmpty()) {

                response.put(
                        "success",
                        false
                );

                response.put(
                        "message",
                        "재료를 한 개 이상 선택해주세요."
                );

                response.put(
                        "recipes",
                        Collections.emptyList()
                );

                return response;
            }


            /*
             * =================================================
             * Vector 검색
             * =================================================
             */
            List<Map<String, Object>> recipes =
                    recipeVectorService.recommendRecipes(
                            ingredients
                    );


            /*
             * 정상 응답
             */
            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    recipes.isEmpty()
                            ? "추천 레시피가 없습니다."
                            : "레시피 추천이 완료되었습니다."
            );

            response.put(
                    "recipes",
                    recipes
            );


            /*
             * 사용자가 선택한 재료도 반환
             */
            response.put(
                    "selectedIngredients",
                    ingredients
            );


            return response;


        } catch (Exception e) {

            /*
             * 서버 로그
             */
            e.printStackTrace();


            /*
             * 오류 응답
             */
            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    "레시피 검색 중 오류가 발생했습니다."
            );

            response.put(
                    "recipes",
                    Collections.emptyList()
            );

            return response;
        }
    }
}

