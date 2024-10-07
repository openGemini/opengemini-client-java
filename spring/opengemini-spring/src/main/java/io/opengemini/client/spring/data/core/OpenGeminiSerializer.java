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

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.QueryResult;

import java.util.List;

/**
 * The serializer between POJO and Point
 *
 * @param <T> class type of POJO annotated by Measurement
 */
public interface OpenGeminiSerializer<T> {

    Point serialize(String measurementName, T pojo) throws OpenGeminiException;

    List<Point> serialize(String measurementName, List<T> pojoList) throws OpenGeminiException;

    List<T> deserialize(String measurementName, QueryResult queryResult) throws OpenGeminiException;
}
