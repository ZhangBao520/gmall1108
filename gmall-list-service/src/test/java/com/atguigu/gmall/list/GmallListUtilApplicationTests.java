package com.atguigu.gmall.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListUtilApplicationTests {

	@Autowired
	JestClient jestClient;

	@Test
	public void testEs() throws IOException {
		String query="{\n" +
				"    \"query\":{\n" +
				"      \"fuzzy\": {\"playerList.name\":\"sad\"}\n" +
				"    }\n" +
				"}";
		Search search = new Search.Builder(query).addIndex("movie_index").addType("movie").build();

		SearchResult result = jestClient.execute(search);

		List<SearchResult.Hit<HashMap, Void>> hits = result.getHits(HashMap.class);

		for (SearchResult.Hit<HashMap, Void> hit : hits) {
			HashMap source = hit.source;
			System.err.println("source = " + source);

		}

	}
	@Test
	public void contextLoads() {
		String password = "123456";
		String pass = DigestUtils.md5Hex(password);
		System.out.println("pass = " + pass);

	}

}
