/*
 * Copyright 2019 Confluent Inc.
 *
 * Licensed under the Confluent Community License; you may not use this file
 * except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.structured.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.confluent.ksql.parser.tree.Expression;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StreamFilter implements ExecutionStep {

  private final ExecutionStepProperties properties;
  private final ExecutionStep source;
  private final Expression filterExpression;

  @JsonCreator
  public StreamFilter(
      @JsonProperty("properties") final ExecutionStepProperties properties,
      @JsonProperty("source") final ExecutionStep source,
      @JsonProperty("filterExpression") final Expression filterExpression) {
    this.properties = Objects.requireNonNull(properties, "properties");
    this.source = Objects.requireNonNull(source, "source");
    this.filterExpression = Objects.requireNonNull(filterExpression, "filterExpression");
  }

  public Expression getFilterExpression() {
    return filterExpression;
  }

  @Override
  public Type getType() {
    return Type.FILTER;
  }

  @Override
  public ExecutionStepProperties getProperties() {
    return properties;
  }

  @Override
  public List<ExecutionStep> getSources() {
    return Collections.singletonList(source);
  }
}
