package com.nowcoder.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaDaoMyBaits implements Alphadao{
    @Override
    public String select() {
        return "MyBatis";
    }
}
