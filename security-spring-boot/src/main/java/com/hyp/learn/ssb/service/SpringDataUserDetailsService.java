package com.hyp.learn.ssb.service;

import com.hyp.learn.ssb.dao.UserDao;
import com.hyp.learn.ssb.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hyp
 * Project name is learn-security
 * Include in com.hyp.learn.ssb.service
 * hyp create at 19-12-19
 **/
@Service
public class SpringDataUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        System.out.println("username=" + s);

        //根据账号去数据库查询...
        UserDto user = userDao.getUserByUsername(s);
        if (user == null) {
            return null;
        }

        //查询用户权限
        List<String> permissions = userDao.findPermissionsByUserId(user.getId());
        String[] perarray = new String[permissions.size()];
        permissions.toArray(perarray);

        //创建userDetails
        UserDetails userDetails =
                User.withUsername(user.getFullname()).password(user.getPassword()).authorities(perarray).build();
        return userDetails;

//        // 这里暂时使用静态数据
//        UserDetails userDetails =
//                User.withUsername(user.getFullname()).password(user.getPassword()).authorities("p1").build();
//        return userDetails;

//        UserDetails user = User
//                .builder()
//                .username("zhangsan")
////                .password("123")
//                .password("$2a$10$1b5mIkehqv5c4KRrX9bUj.A4Y2hug3IGCnMCL5i4RpQrYV12xNKye")
//                .authorities("p1")
//                .build();
//        return user;
    }
}


