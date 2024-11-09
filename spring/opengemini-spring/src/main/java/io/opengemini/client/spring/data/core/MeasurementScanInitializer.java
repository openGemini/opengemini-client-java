/*
 * Copyright 2024 openGemini Authors
 *
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
 */

package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.RpConfig;
import io.opengemini.client.spring.data.annotation.Database;
import io.opengemini.client.spring.data.annotation.Measurement;
import io.opengemini.client.spring.data.annotation.RetentionPolicy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class MeasurementScanInitializer implements InitializingBean {

    private final OpenGeminiTemplate openGeminiTemplate;
    private final MeasurementScanConfigurer measurementScanConfigurer;

    public MeasurementScanInitializer(OpenGeminiTemplate openGeminiTemplate, MeasurementScanConfigurer configurer) {
        this.openGeminiTemplate = openGeminiTemplate;
        this.measurementScanConfigurer = configurer;
    }

    @Override
    public void afterPropertiesSet() {
        Set<Class<?>> measurementClassSet = new HashSet<>();
        if (measurementScanConfigurer != null) {
            for (String basePackage : measurementScanConfigurer.getBasePackages()) {
                scanForMeasurementClass(basePackage, measurementClassSet);
            }
        }
        initDatabase(measurementClassSet);
    }

    private void scanForMeasurementClass(String basePackage, Set<Class<?>> measurementClassSet) {
        if (!StringUtils.hasText(basePackage)) {
            return;
        }

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Measurement.class));
        ClassLoader classLoader = this.getClass().getClassLoader();
        for (BeanDefinition candidate : provider.findCandidateComponents(basePackage)) {
            String beanClassName = candidate.getBeanClassName();
            if (beanClassName != null) {
                try {
                    measurementClassSet.add(ClassUtils.forName(beanClassName, classLoader));
                } catch (ClassNotFoundException | LinkageError ignored) {
                }
            }
        }
    }

    private void initDatabase(Set<Class<?>> measurementClassSet) {
        for (Class<?> clazz : measurementClassSet) {
            Database dbAnnotation = clazz.getAnnotation(Database.class);
            if (dbAnnotation == null || !dbAnnotation.create()) {
                continue;
            }
            String database = dbAnnotation.name();
            openGeminiTemplate.createDatabaseIfAbsent(database);

            RetentionPolicy rpAnnotation = clazz.getAnnotation(RetentionPolicy.class);
            if (rpAnnotation == null || !rpAnnotation.create()) {
                continue;
            }

            RpConfig rpConfig = new RpConfig(rpAnnotation.name(), rpAnnotation.duration(),
                    rpAnnotation.shardGroupDuration(), rpAnnotation.indexDuration());
            openGeminiTemplate.createRetentionPolicyIfAbsent(database, rpConfig, rpAnnotation.isDefault());
        }
    }

}
