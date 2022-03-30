package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    // 替换符号
    private static final String REPLACEMENT = "***";

    // 初始化根节点
    private TrieNode rootNode = new TrieNode();

    // 初始化
    @PostConstruct
    public void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword = reader.readLine()) != null){
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        }catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }


    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for(int i=0; i<keyword.length(); i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode == null){
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点, 进入下一轮循环
            tempNode = subNode;

            // 设置结束标志
            if( i == keyword.length() - 1){
                tempNode.setKeyWordEnd(true);
            }

        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 带过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int postion = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (postion < text.length()){
            char c = text.charAt(postion);

            // 跳过符号
            if(isSymbol(c)){
                // 若指针1 处于根节点，将此符号计入结果，让指针2向下走一步
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                postion++;
                continue;
            }

            // 检测下级结点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                // 以begin字符开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                begin++;
                postion = begin;
                tempNode = rootNode;
            }else if(tempNode.isKeyWordEnd()){
                // 发现敏感词，将begin-postion字符串替换掉
                sb.append(REPLACEMENT);
                postion++;
                begin = postion;
                tempNode = rootNode;
            }else{
                // 检查下一个字符
                postion++;
            }
        }
        // 将最后一批字符加入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }
    // 判断是否为符号
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    // 前缀树
    private class TrieNode{

        // 关键词结束标识
        private  boolean isKeyWordEnd = false;

        // 子节点
        private Map<Character, TrieNode>  subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点方法
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c, node);
        }

        //获取子节点方法
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
