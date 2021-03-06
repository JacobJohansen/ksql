/*
 * Copyright 2019 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"; you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.rest.server.resources;

import com.google.common.collect.ImmutableMap;
import io.confluent.ksql.rest.entity.ClusterStatusResponse;
import io.confluent.ksql.rest.entity.HostStatusEntity;
import io.confluent.ksql.rest.entity.HostStoreLags;
import io.confluent.ksql.rest.entity.KsqlHostEntity;
import io.confluent.ksql.rest.entity.Versions;
import io.confluent.ksql.rest.server.HeartbeatAgent;
import io.confluent.ksql.rest.server.LagReportingAgent;
import io.confluent.ksql.util.HostStatus;
import io.confluent.ksql.util.KsqlHost;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Endpoint that reports the view of the cluster that this server has.
 * Returns every host that has been discovered by this server along side with information about its
 * status such as whether it is alive or dead and the last time its status got updated.
 */

@Path("/clusterStatus")
@Produces({Versions.KSQL_V1_JSON, MediaType.APPLICATION_JSON})
public class ClusterStatusResource {

  private final HeartbeatAgent heartbeatAgent;
  private final Optional<LagReportingAgent> lagReportingAgent;
  private static final HostStoreLags EMPTY_HOST_STORE_LAGS =
      new HostStoreLags(ImmutableMap.of(), 0);

  public ClusterStatusResource(final HeartbeatAgent heartbeatAgent,
                               final Optional<LagReportingAgent> lagReportingAgent) {
    this.heartbeatAgent = heartbeatAgent;
    this.lagReportingAgent = lagReportingAgent;
  }

  @GET
  public Response checkClusterStatus() {
    final ClusterStatusResponse response = getResponse();
    return Response.ok(response).build();
  }

  private ClusterStatusResponse getResponse() {
    final Map<KsqlHost, HostStatus> allHostStatus = heartbeatAgent.getHostsStatus();

    final Map<KsqlHostEntity, HostStatusEntity> response = allHostStatus
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> new KsqlHostEntity(entry.getKey().host(), entry.getKey().port()) ,
            entry -> new HostStatusEntity(entry.getValue().isHostAlive(),
                                          entry.getValue().getLastStatusUpdateMs(),
                                          getHostStoreLags(entry.getKey()))
        ));

    return new ClusterStatusResponse(response);
  }

  private HostStoreLags getHostStoreLags(final KsqlHost ksqlHost) {
    return lagReportingAgent
      .flatMap(agent -> agent.getLagPerHost(ksqlHost))
        .orElse(EMPTY_HOST_STORE_LAGS);
  }
}
