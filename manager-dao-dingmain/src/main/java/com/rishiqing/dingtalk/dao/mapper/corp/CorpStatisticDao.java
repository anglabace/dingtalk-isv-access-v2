package com.rishiqing.dingtalk.dao.mapper.corp;

import com.rishiqing.dingtalk.dao.model.corp.CorpStatisticDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Wallace Mao
 * Date: 2018-12-11 18:57
 */
@Repository("corpStatisticDao")
public interface CorpStatisticDao {
    void saveOrUpdateCorpStatistic(CorpStatisticDO corpStatisticDO);

    CorpStatisticDO getCorpStatisticByCorpId(@Param("corpId") String corpId);

    CorpStatisticDO getCorpStatisticByCorpIdForUpdate(@Param("corpId") String corpId);
}
