package com.wei.gulimall.member.dao;

import com.wei.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author wei
 * @email 2558939179qq@gmail.com
 * @date 2022-09-05 15:43:59
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
