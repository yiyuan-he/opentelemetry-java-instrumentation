/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.awssdk.v2_2;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

final class AwsExperimentalAttributes {
  static final AttributeKey<String> AWS_BUCKET_NAME = stringKey("aws.bucket.name");
  static final AttributeKey<String> AWS_QUEUE_URL = stringKey("aws.queue.url");
  static final AttributeKey<String> AWS_QUEUE_NAME = stringKey("aws.queue.name");
  static final AttributeKey<String> AWS_STREAM_NAME = stringKey("aws.stream.name");
  static final AttributeKey<String> AWS_TABLE_NAME = stringKey("aws.table.name");
  static final AttributeKey<String> AWS_BEDROCK_GUARDRAIL_ID =
      stringKey("aws.bedrock.guardrail_id");
  static final AttributeKey<String> AWS_BEDROCK_AGENT_ID = stringKey("aws.bedrock.agent_id");
  static final AttributeKey<String> AWS_BEDROCK_DATASOURCE_ID =
      stringKey("aws.bedrock.data_source_id");
  static final AttributeKey<String> AWS_BEDROCK_KNOWLEDGEBASE_ID =
      stringKey("aws.bedrock.knowledge_base_id");

  // OTel GenAI/LLM group has defined gen_ai attributes but not yet add it in
  // semantic-conventions-java package
  // https://github.com/open-telemetry/semantic-conventions/blob/main/docs/gen-ai/gen-ai-spans.md#genai-attributes
  // TODO: Merge in these attributes in semantic-conventions-java package
  static final AttributeKey<String> GEN_AI_MODEL = stringKey("gen_ai.request.model");

  private AwsExperimentalAttributes() {}
}
