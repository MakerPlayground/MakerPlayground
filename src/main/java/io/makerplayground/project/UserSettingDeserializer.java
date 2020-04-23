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

package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.Record;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.project.expression.*;
import io.makerplayground.project.term.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class UserSettingDeserializer extends JsonDeserializer<UserSetting> {

    private final Project project;
    private final ObjectMapper mapper = new ObjectMapper();

    UserSettingDeserializer(Project project) {
        this.project = project;
    }

    @Override
    public UserSetting deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = mapper.readTree(jsonParser);

        String deviceName = node.get("device").asText();
        ProjectDevice projectDevice = null;
        for (ProjectDevice pd : project.getUnmodifiableProjectDevice()) {
            if (pd.getName().equals(deviceName)) {
                projectDevice = pd;
                break;
            }
        }
        for (ProjectDevice pd : VirtualProjectDevice.virtualDevices) {
            if (pd.getName().equals(deviceName)) {
                projectDevice = pd;
                break;
            }
        }
        if (projectDevice == null) {
            throw new IllegalStateException("Cannot parse mp file because no support device.");
        }

        boolean hasAction = node.has("action");
        boolean hasCondition = node.has("condition");
        Action action = node.has("action")
                ? projectDevice.getGenericDevice().getAction(node.get("action").asText()).orElse(null)
                : null;
        io.makerplayground.device.shared.Condition condition = node.has("condition")
                ? projectDevice.getGenericDevice().getCondition(node.get("condition").asText()).orElse(null)
                : null;
        if (action == null && condition == null) {
            throw new IllegalStateException("UserSetting must contain action or condition");
        }

        Map<Parameter, Expression> valueMap = new HashMap<>();
        for (JsonNode parameterNode : node.get("valueMap")) {
            Parameter parameter = null;
            if (hasAction) {
                parameter = action.getParameter(parameterNode.get("name").asText()).orElseThrow();
            }
            if (hasCondition) {
                parameter = condition.getParameter(parameterNode.get("name").asText()).orElseThrow();
            }
            assert parameter != null;
            Expression expression;
            String expressionType = parameterNode.get("type").asText();
            JsonNode valueNode = parameterNode.get("value");
            List<Term> terms = new ArrayList<>();
            for (JsonNode term_node : valueNode.get("terms")) {
                terms.add(deserializeTerm(mapper, parameter, term_node, project.getUnmodifiableProjectDevice()));
            }
            if (ProjectValueExpression.class.getSimpleName().equals(expressionType)) {
                expression = new ProjectValueExpression(((ValueTerm) terms.get(0)).getValue());
            } else if (CustomNumberExpression.class.getSimpleName().equals(expressionType)) {
                expression = new CustomNumberExpression(terms);
            } else if (NumberWithUnitExpression.class.getSimpleName().equals(expressionType)) {
                expression = new NumberWithUnitExpression(((NumberWithUnitTerm) terms.get(0)).getValue());
            } else if (SimpleStringExpression.class.getSimpleName().equals(expressionType)) {
                expression = new SimpleStringExpression(((StringTerm) terms.get(0)).getValue());
            } else if (ValueLinkingExpression.class.getSimpleName().equals(expressionType)) {
                boolean inverse = false;
                if (valueNode.has("inverse")) {
                    inverse = valueNode.get("inverse").asBoolean();
                }
                expression = new ValueLinkingExpression(parameter, terms, inverse);
            } else if (SimpleRTCExpression.class.getSimpleName().equals(expressionType)) {
                expression = new SimpleRTCExpression(((RTCTerm) (terms.get(0))).getValue());
            } else if (ImageExpression.class.getSimpleName().equals(expressionType)) {
                expression = new ImageExpression(((ValueTerm) terms.get(0)).getValue());
            } else if (RecordExpression.class.getSimpleName().equals(expressionType)) {
                expression = new RecordExpression(((RecordTerm) terms.get(0)).getValue());
            } else if (ComplexStringExpression.class.getSimpleName().equals(expressionType)) {
                expression = new ComplexStringExpression(terms);
            } else if (SimpleIntegerExpression.class.getSimpleName().equals(expressionType)) {
                expression = new SimpleIntegerExpression(((IntegerTerm)(terms.get(0))).getValue());
            } else if (StringIntegerExpression.class.getSimpleName().equals(expressionType)) {
                String key = ((StringTerm) terms.get(0)).getValue();
                expression = new StringIntegerExpression((StringIntegerCategoricalConstraint) parameter.getConstraint(), key);
            } else if (DotMatrixExpression.class.getSimpleName().equals(expressionType)) {
                DotMatrix dotMatrix = ((DotMatrixTerm) terms.get(0)).getValue();
                expression = new DotMatrixExpression(dotMatrix);
            }
            else {
                throw new IllegalStateException("expression type [" + expressionType + "] is not supported");
            }

            Expression.RefreshInterval refreshInterval = Expression.RefreshInterval.valueOf(valueNode.get("refreshInterval").asText());
            NumberWithUnit interval = mapper.readValue(valueNode.get("userDefinedInterval").traverse()
                    , new TypeReference<NumberWithUnit>(){});
            expression.setRefreshInterval(refreshInterval);
            expression.setUserDefinedInterval(interval);
            valueMap.put(parameter, expression);
        }

        Map<Value, Expression> expressionMap = new HashMap<>();
        Map<Value, Boolean> expressionEnableMap = new HashMap<>();
        for (JsonNode valueNode : node.get("expression")) {
            Value value = projectDevice.getGenericDevice().getValue(valueNode.get("name").asText()).orElseThrow();
            boolean enable = valueNode.get("enable").asBoolean();
            String type = valueNode.get("type").asText();
            List<Term> terms = new ArrayList<>();
            for (JsonNode term_node : valueNode.get("expression")) {
                terms.add(deserializeTerm(mapper, null, term_node, project.getUnmodifiableProjectDevice()));
            }
            Expression expression;
            if (NumberInRangeExpression.class.getName().contains(type)) {
                expression = deserializeNumberInRangeExpression(valueNode.get("expression"), projectDevice, value);
            } else if (ConditionalExpression.class.getName().contains(type)) {
                expression = new ConditionalExpression(projectDevice, value, terms);
            } else {
                throw new IllegalStateException("Unknown expression type");
            }

            expressionMap.put(value, expression);
            expressionEnableMap.put(value, enable);
        }
        if (hasAction) {
            return new UserSetting(projectDevice, action, valueMap, expressionMap, expressionEnableMap);
        }
        if (hasCondition) {
            return new UserSetting(projectDevice, condition, valueMap, expressionMap, expressionEnableMap);
        }
        throw new IllegalStateException("UserSetting Deserializer Error");
    }

    private Expression deserializeNumberInRangeExpression(JsonNode node, ProjectDevice device, Value value) {
        if (node.get(0).get("type").asText().equals(Term.Type.VALUE.name())
                && node.get(1).get("type").asText().equals(Term.Type.OPERATOR.name())
                && (node.get(1).get("value").asText().equals(Operator.LESS_THAN.name())
                || node.get(1).get("value").asText().equals(Operator.LESS_THAN_OR_EQUAL.name()))
                && node.get(2).get("type").asText().equals(Term.Type.NUMBER.name())
                && node.get(3).get("type").asText().equals(Term.Type.OPERATOR.name())
                && node.get(3).get("value").asText().equals(Operator.AND.name())
                && node.get(4).get("type").asText().equals(Term.Type.VALUE.name())
                && node.get(5).get("type").asText().equals(Term.Type.OPERATOR.name())
                && (node.get(5).get("value").asText().equals(Operator.GREATER_THAN.name())
                || node.get(5).get("value").asText().equals(Operator.GREATER_THAN_OR_EQUAL.name()))
                && node.get(6).get("type").asText().equals(Term.Type.NUMBER.name())) {
            return new NumberInRangeExpression(device, value)
                    .setLowValue(node.get(6).get("value").get("value").asDouble())
                    .setHighValue(node.get(2).get("value").get("value").asDouble())
                    .setLowOperator(Operator.valueOf(node.get(5).get("value").asText()))
                    .setHighOperator(Operator.valueOf(node.get(1).get("value").asText()));
        } else {
            throw new IllegalStateException("Simple expression parsing fail");
        }
    }

    private Term deserializeTerm(ObjectMapper mapper, Parameter parameter, JsonNode term_node, Collection<ProjectDevice> allProjectDevices) throws IOException {
        String term_type = term_node.get("type").asText();
        Term term;
        if (Term.Type.NUMBER.name().equals(term_type)) {
            double num = term_node.get("value").get("value").asDouble();
            Unit unit = Unit.valueOf(term_node.get("value").get("unit").asText());
            NumberWithUnit numberWithUnit = new NumberWithUnit(num, unit);
            term = new NumberWithUnitTerm(numberWithUnit);
        } else if (Term.Type.OPERATOR.name().equals(term_type)) {
            String operator = term_node.get("value").asText();
            term = new OperatorTerm(Operator.valueOf(operator));
        } else if (Term.Type.VALUE.name().equals(term_type)) {
            if ("null".equals(term_node.get("value").asText())) {
                term = new ValueTerm(null);
            } else {
                String projectDeviceName = term_node.get("value").get("name").asText();
                String valueName = term_node.get("value").get("value").asText();
                Optional<ProjectDevice> deviceOptional = allProjectDevices.stream().filter(pj -> pj.getName().equals(projectDeviceName)).findFirst();
                if (deviceOptional.isEmpty()) {
                    throw new IllegalStateException("projectDevice for term is needed to be existed.");
                }
                ProjectDevice device = deviceOptional.get();
                Value value = device.getGenericDevice().getValue(valueName).orElseThrow();
                term = new ValueTerm(new ProjectValue(device, value));
            }
        } else if (Term.Type.STRING.name().equals(term_type)) {
            String word = term_node.get("value").asText();
            term = new StringTerm(word);
        } else if (Term.Type.DATETIME.name().equals(term_type)) {
            JsonNode temp_node = term_node.get("value").get("localDateTime");
            int year = temp_node.get("year").asInt();
            int month = temp_node.get("monthValue").asInt();
            int day = temp_node.get("dayOfMonth").asInt();
            int hour = temp_node.get("hour").asInt();
            int minute = temp_node.get("minute").asInt();
            int second = temp_node.get("second").asInt();
            LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
            RealTimeClock.Mode mode = RealTimeClock.Mode.valueOf(term_node.get("value").get("mode").asText());
            term = new RTCTerm(new RealTimeClock(mode, localDateTime));
        } else if (Term.Type.RECORD.name().equals(term_type)) {
            List<RecordEntry> recordEntryList = new ArrayList<>();
            for (JsonNode entryNode : term_node.get("value").get("entryList")) {
                String fieldName = entryNode.get("field").asText();
                Expression expression = deserializeExpression(mapper, parameter, entryNode, allProjectDevices);
                recordEntryList.add(new RecordEntry(fieldName, expression));
            }
            term = new RecordTerm(new Record(recordEntryList));
        } else if (Term.Type.NUMBER_ONLY.name().equals(term_type)) {
            int integer = term_node.get("value").asInt();
            term = new IntegerTerm(integer);
        } else if (Term.Type.DOT_MATRIX.name().equals(term_type)) {
            int row = term_node.get("value").get("row").asInt();
            int column = term_node.get("value").get("column").asInt();
            String data = term_node.get("value").get("data").asText();
            DotMatrix dotMatrix = new DotMatrix(row, column, data);
            term = new DotMatrixTerm(dotMatrix);
        }
        else {
            throw new IllegalStateException("deserialize unsupported term");
        }
        return term;
    }

    private Expression deserializeExpression(ObjectMapper mapper, Parameter parameter, JsonNode parameterNode
            , Collection<ProjectDevice> allProjectDevices) throws IOException {
        Expression expression;
        String expressionType = parameterNode.get("type").asText();
        JsonNode valueNode = parameterNode.get("value");
        List<Term> terms = new ArrayList<>();
        for (JsonNode term_node : valueNode.get("terms")) {
            terms.add(deserializeTerm(mapper, parameter, term_node, allProjectDevices));
        }
        if (ProjectValueExpression.class.getSimpleName().equals(expressionType)) {
            expression = new ProjectValueExpression(((ValueTerm) terms.get(0)).getValue());
        } else if (CustomNumberExpression.class.getSimpleName().equals(expressionType)) {
            expression = new CustomNumberExpression(terms);
        } else if (NumberWithUnitExpression.class.getSimpleName().equals(expressionType)) {
            expression = new NumberWithUnitExpression(((NumberWithUnitTerm) terms.get(0)).getValue());
        } else if (SimpleStringExpression.class.getSimpleName().equals(expressionType)) {
            expression = new SimpleStringExpression(((StringTerm) terms.get(0)).getValue());
        } else if (ValueLinkingExpression.class.getSimpleName().equals(expressionType)){
            boolean inverse = false;
            if (valueNode.has("inverse")) {
                inverse = valueNode.get("inverse").asBoolean();
            }
            expression = new ValueLinkingExpression(parameter, terms, inverse);
        } else if (SimpleRTCExpression.class.getSimpleName().equals(expressionType)) {
            expression = new SimpleRTCExpression(((RTCTerm)(terms.get(0))).getValue());
        } else if (RecordExpression.class.getSimpleName().equals(expressionType)) {
            expression = new RecordExpression(((RecordTerm)(terms.get(0))).getValue());
        } else if (SimpleIntegerExpression.class.getSimpleName().equals(expressionType)) {
            expression = new SimpleIntegerExpression(((IntegerTerm)(terms.get(0))).getValue());
        } else {
            throw new IllegalStateException("expression type not supported");
        }

        Expression.RefreshInterval refreshInterval = Expression.RefreshInterval.valueOf(valueNode.get("refreshInterval").asText());
        NumberWithUnit interval = mapper.readValue(valueNode.get("userDefinedInterval").traverse()
                , new TypeReference<NumberWithUnit>(){});
        expression.setRefreshInterval(refreshInterval);
        expression.setUserDefinedInterval(interval);
        return expression;
    }
}
