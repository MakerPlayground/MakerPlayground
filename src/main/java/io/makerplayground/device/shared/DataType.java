/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device.shared;

/**
 * An enum represent type of data
 */
public enum DataType {
    DOUBLE, STRING, INTEGER, INTEGER_ENUM, BOOLEAN_ENUM, STRING_ENUM, STRING_INT_ENUM, DATETIME, IMAGE, AZURE_COGNITIVE_KEY, AZURE_IOTHUB_KEY, VARIABLE_NAME, RECORD, DOT_MATRIX_DATA, RGB_DOT_MATRIX_DATA, K210_OBJDETECT_MODEL, BOOLEAN_EXPRESSION
    // TODO: add new data type
}
