package com.nowcoder.community.services;

import com.nowcoder.community.dao.Alphadao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private Alphadao alphadao;

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }

    @PostConstruct//被该注解注释的方法会在构造后调用
    public void init(){
        System.out.println("初始化AlphaService");
    }
    @PreDestroy//销毁前调用该方法
    public void destory(){
        System.out.println("销毁AlphaService");
    }

    public String find(){
        return alphadao.select();
    }

}
