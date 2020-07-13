package com.ltz.coupon.dao;

import com.ltz.coupon.entity.RolePathMapping;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <h1>RolePathMapping Dao</h1>
 */
public interface RolePathMappingRepository
        extends JpaRepository<RolePathMapping, Integer> {

    /**
     * <h2>通过 角色id + 路径id 寻找数据记录</h2>
     * */
    RolePathMapping findByRoleIdAndPathId(Integer roleId, Integer pathId);
}
