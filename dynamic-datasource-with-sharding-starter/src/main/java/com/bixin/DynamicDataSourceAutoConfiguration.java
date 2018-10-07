/**
 * Copyright © 2018 organization baomidou
 * <pre>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <pre/>
 */
package com.bixin;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.YmlDynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import com.bixin.DynamicDataSource.MixDynamicDataSourceProvider;
import com.bixin.shardingSphere.SpringBootMasterSlaveRuleConfigurationProperties;
import com.bixin.shardingSphere.SpringBootShardingRuleConfigurationProperties;
import com.bixin.util.PropertyUtil;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.api.MasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import io.shardingsphere.core.util.DataSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 动态数据源核心自动配置类
 *
 * @author TaoYu Kanyuxia, Harry Chen
 * @see DynamicDataSourceProvider
 * @see DynamicDataSourceStrategy
 * @see DynamicRoutingDataSource
 * @see ShardingDataSource
 * @see MasterSlaveDataSource
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties({DynamicDataSourceProperties.class,
        SpringBootShardingRuleConfigurationProperties.class,    //from sharding-sphere
        SpringBootMasterSlaveRuleConfigurationProperties.class})    //from sharding-sphere
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import(DruidDynamicDataSourceConfiguration.class)
public class DynamicDataSourceAutoConfiguration implements EnvironmentAware, CommandLineRunner {

    @Value("${sharding.jdbc.datasource.worker.id:#{null}}")
    private Long workerId;

    @Autowired private DynamicDataSourceProperties properties;

    //from sharding-sphere
    @Autowired private SpringBootShardingRuleConfigurationProperties shardingProperties;
    //from sharding-sphere
    @Autowired private SpringBootMasterSlaveRuleConfigurationProperties masterSlaveProperties;
    //from sharding-sphere
    private final Map<String, DataSource> shardingDataSourceMap = new LinkedHashMap<>();

    @Bean("dynamicDataSourceProvider")
    public DynamicDataSourceProvider dynamicDataSourceProvider() throws SQLException {
        DataSource shardingDataSource = this.getShardingDataSource();
        return new MixDynamicDataSourceProvider(properties, shardingDataSource);
    }

    @Bean
    public DynamicRoutingDataSource dynamicRoutingDataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        dynamicRoutingDataSource.setPrimary(properties.getPrimary());
        dynamicRoutingDataSource.setStrategy(properties.getStrategy());
        dynamicRoutingDataSource.setProvider(dynamicDataSourceProvider);
        return dynamicRoutingDataSource;
    }

    @Bean
    public DynamicDataSourceAnnotationAdvisor dynamicDatasourceAnnotationAdvisor() {
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(properties.isMpEnabled());
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor);
        advisor.setOrder(properties.getOrder());
        return advisor;
    }

    //from sharding-sphere
    @Override
    public final void setEnvironment(Environment environment) {
        this.setDataSourceMap(environment);
    }

    //from sharding-sphere
    @SuppressWarnings("unchecked")
    private void setDataSourceMap(final Environment environment) {
        String prefix = "sharding.jdbc.datasource.";
        String dataSources = environment.getProperty(prefix + "names");
        for (String each : dataSources.split(",")) {
            try {
                Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + each, Map.class);
                Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");
                DataSource dataSource = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
                shardingDataSourceMap.put(each, dataSource);
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingException("Can't find datasource type!", ex);
            }
        }
    }

    //from sharding-sphere
    private DataSource getShardingDataSource() throws SQLException {
        return null == masterSlaveProperties.getMasterDataSourceName()
                ? ShardingDataSourceFactory.createDataSource(this.shardingDataSourceMap, shardingProperties.getShardingRuleConfiguration(), shardingProperties.getConfigMap(), shardingProperties.getProps())
                : MasterSlaveDataSourceFactory.createDataSource(
                this.shardingDataSourceMap, masterSlaveProperties.getMasterSlaveRuleConfiguration(), masterSlaveProperties.getConfigMap(), masterSlaveProperties.getProps());
    }

    @Override
    public void run(String... strings) throws Exception {
        /* init workerId for DefaultKeyGenerator */
        if ( null != workerId ) {
            DefaultKeyGenerator.setWorkerId( workerId );
        }
        else {
            /* 根据机器IP获取工作进程编号。
               如果线上机器的IP二进制表示的最后10位不重复,建议使用此种方式。
            */
            InetAddress address;
            try {
                address = InetAddress.getLocalHost();
            } catch (final UnknownHostException e) {
                throw new IllegalStateException("Cannot get LocalHost InetAddress, please check your network!");
            }
            byte[] ipAddressByteArray = address.getAddress();
            DefaultKeyGenerator.setWorkerId((long) (((ipAddressByteArray[ipAddressByteArray.length - 2] & 0B11) << Byte.SIZE)
                    + (ipAddressByteArray[ipAddressByteArray.length - 1] & 0xFF)));
        }
    }
}