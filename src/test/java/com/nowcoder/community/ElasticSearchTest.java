package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate restTemplate;



    @Test
    public void testInsert(){
        discussRepository.save(discussMapper.selectDiscussPostById(241));
        discussRepository.save(discussMapper.selectDiscussPostById(242));
        discussRepository.save(discussMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertLList(){
        discussRepository.saveAll(discussMapper.selectDiscussPosts(103, 0, 100));
    }

    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussMapper.selectDiscussPostById(217);
        discussPost.setContent("新人驾到，灌水无敌！");
        discussRepository.save(discussPost);
    }

    @Test
    public void testDelete(){
        discussRepository.deleteById(217);
    }

    @Test
    public void testSearchByRepositytory(){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC), SortBuilders.fieldSort("score").order(SortOrder.DESC), SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> searchHits = restTemplate.search(searchQuery, DiscussPost.class);
        List<SearchHit<DiscussPost>> searchHitList = searchHits.toList();
        System.out.println(searchHits.getTotalHits());
        System.out.println(searchHits.stream().count());
        List<DiscussPost> list = new ArrayList<>();
        for(SearchHit<DiscussPost> searchHit : searchHits){
            DiscussPost post = searchHit.getContent();

            List<String> titleList =  searchHit.getHighlightFields().get("title");
            if(titleList != null){
                post.setTitle(titleList.get(0).toString());
            }

            List<String> contentList = searchHit.getHighlightFields().get("content");
            if(contentList != null) {
                post.setContent(contentList.get(0).toString());
            }
            list.add(post);
        }

        for(DiscussPost discussPost : list){
            System.out.println(discussPost);
        }
    }
}
