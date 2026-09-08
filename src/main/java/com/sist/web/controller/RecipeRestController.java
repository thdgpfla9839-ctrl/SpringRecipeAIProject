package com.sist.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


/*
 *  전체구조 html => 사용자가 재료 선택
 *                     |
 *                     RecipeController => 화면 출력
 *                     |
 *                     재료 선택
 *                     RecipeRestController
 *                     |
 *                     1) RecipeService
 *                     2) Mapper
 *                     => 두가지로 연결
 *                     |
 *                     EmbeddingModel
 *                     => String -> float[]
 *                     |
 *                     vector : [0.1,0.2 ......]  이 데이터를 float으로 변환해서 가져온다
 *                     |
 *                     Mapper => findSimilarRecipe()
 *                     |
 *                     PostgreSQL + pgVector
 *                     => embedding을 이용해 백터 검색
 *                        Cosine Distance를 이용해 가장 가까운 거리 측정(실수를 이용 => 모든 문자는 실수로 돼 있음)
 *                     |
 *                     distance => 거리가 작은 순으로 추출   
 *                     |
 *                     ORDER BY => LIMIT 5
 *                     |
 *                     재료 비교
 *                     |
 *                     AVA (보유) / SHO (부족) / SUB (대체)
 *                     |
 *                     ingredientRate
 *                     ingredients
 *                     missingingredient
 *                     |
 *                     thymeleaf로 출력하든지 => 충족률 : 75% ... 이런 식으로 구현할 예정
 *                                           없는 재료는 재료X 형식으로 출력될 것
 *                     
 */
@Controller
public class RecipeRestController {

	@GetMapping("/recipe/recommand")
	public String recipe_recommand()
	{
		return "recipe/recommand";
	}
}
