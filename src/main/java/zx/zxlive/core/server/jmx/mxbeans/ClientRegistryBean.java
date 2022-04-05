/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package zx.zxlive.core.server.jmx.mxbeans;

import java.util.List;

import zx.zxlive.core.server.Client;
import zx.zxlive.core.server.exception.ClientNotFoundException;

/**
 * An MBean interface for the client registry.
 *
 * @author The zSharpe Project
 * @author Vic Wang (xiaoyu860912@163.com)
 */
//@MXBean
public interface ClientRegistryBean {

    public String nextId();

    public String previousId();

    public boolean hasClient(String id);

    public List<Client> getClientList();

    public Client getClient(String id) throws ClientNotFoundException;

}