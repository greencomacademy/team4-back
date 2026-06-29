package com.deliveryinsider.domain.auth.mapper;

import com.deliveryinsider.domain.user.entities.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
//    이메일 중복 여부 확인
    int countByEmail(String email);

//    회원 저장
    int save(User user);

    int updateRefreshToken(
            @Param("id") Long id,
            @Param("refreshToken") String refreshToken
    );

    int deleteRefreshToken(Long id);

    int countByEmailExceptId(
            @Param("email") String email,
            @Param("id") Long id
    );

    int updateEmail(
            @Param("id") Long id,
            @Param("email") String email
    );

//    회원 ID로 조회
    User findById(Long id);

//    이메일로 회원 조회
    User findByEmail(String email);


    User findByRefreshToken(String refreshToken);

}
