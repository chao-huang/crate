/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.operation.fetch;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.MoreExecutors;
import io.crate.jobs.JobContextService;
import io.crate.operation.collect.stats.JobsLogs;
import io.crate.test.integration.CrateUnitTest;
import org.elasticsearch.common.breaker.NoopCircuitBreaker;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.cluster.NoopClusterService;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;

public class NodeFetchOperationTest extends CrateUnitTest {

    @Test
    public void testSysOperationsIsClearedIfNothingToFetch() throws Exception {
        JobsLogs jobsLogs = new JobsLogs(() -> true);
        NodeFetchOperation fetchOperation = new NodeFetchOperation(
            MoreExecutors.directExecutor(),
            jobsLogs,
            new JobContextService(Settings.EMPTY, new NoopClusterService(), jobsLogs),
            new NoopCircuitBreaker("dummy")
            );

        fetchOperation.fetch(UUID.randomUUID(), 1, null, true).get(5, TimeUnit.SECONDS);

        assertThat(Iterables.size(jobsLogs.activeOperations()), is(0));
    }
}
