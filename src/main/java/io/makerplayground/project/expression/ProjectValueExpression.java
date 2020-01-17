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

package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.ValueTerm;

public class ProjectValueExpression extends Expression {

    public ProjectValueExpression() {
        super(Type.PROJECT_VALUE);
        terms.add(new ValueTerm(null));
    }

    public ProjectValueExpression(ProjectValue projectValue) {
        super(Type.PROJECT_VALUE);
        terms.add(new ValueTerm(projectValue));
    }

    ProjectValueExpression(ProjectValueExpression e) {
        super(e);
    }

    @JsonIgnore
    public ProjectValue getProjectValue() {
        return ((ValueTerm) terms.get(0)).getValue();
    }

    public ProjectValueExpression setProjectValue(ProjectValue projectValue) {
        ProjectValueExpression expression = new ProjectValueExpression(this);
        expression.terms.set(0, new ValueTerm(projectValue));
        return expression;
    }

    @Override
    public ProjectValueExpression deepCopy() {
        return new ProjectValueExpression(this);
    }
}
