package com.dynamic.server.datasourceconfig;

import com.google.common.base.Strings;
import io.shardingsphere.core.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;

/**
 * Created by Harry on 2018/9/27.
 */
public class UserIdShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
        if (Strings.isNullOrEmpty( preciseShardingValue.getValue() )) return collection.iterator().next();
        int index = preciseShardingValue.getValue().hashCode() % 16;
        if ( index < 0 ) index += 16;
        return preciseShardingValue.getLogicTableName()+"_"+index;
    }
}
