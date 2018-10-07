package com.bixin.DynamicDataSource;

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.bixin.DataSourceType;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.baomidou.dynamic.datasource.toolkit.DataSourceFactory.createDataSource;

/**
 * Created by Harry on 2018/9/28.
 */
@Slf4j
public class MixDynamicDataSourceProvider implements DynamicDataSourceProvider {

    private DynamicDataSourceProperties properties;
    private DataSource shardingDataSource;
    public MixDynamicDataSourceProvider(DynamicDataSourceProperties properties, DataSource shardingDataSource) {
        this.properties = properties;
        this.shardingDataSource = shardingDataSource;
    }

    @Override
    public Map<String, DataSource> loadDataSources() {
        Map<String, DataSourceProperty> dataSourcePropertiesMap = properties.getDatasource();
        Map<String, DataSource> dataSourceMap = new HashMap<>(dataSourcePropertiesMap.size());
        for (Map.Entry<String, DataSourceProperty> item : dataSourcePropertiesMap.entrySet()) {
            dataSourceMap.put(item.getKey(), createDataSource(item.getValue()));
        }
        if ( null != shardingDataSource ) {
            dataSourceMap.put(DataSourceType.SHARDING_DATASOURCE, shardingDataSource);
        }
        else {
            log.info("no sharding datasource");
        }
        return dataSourceMap;
    }
}
