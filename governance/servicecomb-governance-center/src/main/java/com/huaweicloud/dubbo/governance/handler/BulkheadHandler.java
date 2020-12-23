/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.dubbo.governance.handler;

import com.huaweicloud.dubbo.governance.policy.BulkheadPolicy;
import com.huaweicloud.dubbo.governance.policy.Policy;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;

import java.time.Duration;

public class BulkheadHandler extends AbstractGovHandler<Bulkhead> {

  @Override
  public DecorateCheckedSupplier<?> process(DecorateCheckedSupplier<?> supplier, Policy policy) {
    Bulkhead bulkhead = getActuator(policy.name(), (BulkheadPolicy) policy, this::getBulkhead);
    return supplier.withBulkhead(bulkhead);
  }

  @Override
  public HandlerType type() {
    return HandlerType.SERVER;
  }

  private Bulkhead getBulkhead(BulkheadPolicy policy) {
    BulkheadConfig config = BulkheadConfig.custom()
        .maxConcurrentCalls(policy.getMaxConcurrentCalls())
        .maxWaitDuration(Duration.ofMillis(policy.getMaxWaitDuration()))
        .build();

    BulkheadRegistry registry = BulkheadRegistry.of(config);

    return registry.bulkhead(policy.name());
  }
}