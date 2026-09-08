package com.sist.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;
import com.sist.web.vo.*;
@Mapper
public interface PostgresMapper {

	/** * ======================================================== * pgVector 유사 레시피 검색 * ======================================================== * 
	 * * @param embedding 검색용 embedding * @param limit 가져올 레시피 개수 * 
	 * * @return 유사 레시피 목록 
	 * */ 
	public List<Map<String, Object>> findSimilarRecipes( 
			@Param("embedding") String embedding, 
			@Param("limit") int limit );
}
