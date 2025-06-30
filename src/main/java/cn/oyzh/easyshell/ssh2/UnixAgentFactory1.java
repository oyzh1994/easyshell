/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.oyzh.easyshell.ssh2;

import com.jcraft.jsch.AgentProxyException;
import org.apache.sshd.agent.SshAgent;
import org.apache.sshd.agent.SshAgentFactory;
import org.apache.sshd.agent.SshAgentServer;
import org.apache.sshd.agent.unix.ChannelAgentForwardingFactory;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.channel.ChannelFactory;
import org.apache.sshd.common.session.ConnectionService;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.threads.CloseableExecutorService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class UnixAgentFactory1 implements SshAgentFactory {
    public static final List<ChannelFactory> DEFAULT_FORWARDING_CHANNELS = Collections.unmodifiableList(
            Arrays.asList(
                    ChannelAgentForwardingFactory.OPENSSH,
                    ChannelAgentForwardingFactory.IETF));

    private Factory<CloseableExecutorService> executorServiceFactory;

    public UnixAgentFactory1() {
        super();
    }

    public UnixAgentFactory1(Factory<CloseableExecutorService> factory) {
        executorServiceFactory = factory;
    }

    protected CloseableExecutorService newExecutor() {
        return executorServiceFactory != null ? executorServiceFactory.create() : null;
    }

    @Override
    public List<ChannelFactory> getChannelForwardingFactories(FactoryManager manager) {
        if (executorServiceFactory != null) {
            return DEFAULT_FORWARDING_CHANNELS.stream()
                    .map(cf -> new ChannelAgentForwardingFactory(cf.getName(), executorServiceFactory))
                    .collect(Collectors.toList());
        } else {
            return DEFAULT_FORWARDING_CHANNELS;
        }
    }

    @Override
    public SshAgent createClient(Session session, FactoryManager manager) throws IOException {
        try {
            return new UnixSSHAgent1();
        } catch (AgentProxyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SshAgentServer createServer(ConnectionService service) throws IOException {
        throw new UnsupportedOperationException();
    }
}
