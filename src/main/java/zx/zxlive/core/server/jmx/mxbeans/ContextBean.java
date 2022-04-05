/*
 * RED5 Open Source Media Server - https://github.com/Red5/ Copyright 2006-2016 by respective authors (see below). All rights reserved. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package zx.zxlive.core.server.jmx.mxbeans;

import java.io.IOException;

import zx.zxlive.core.server.api.scope.IScopeHandler;
import zx.zxlive.core.server.api.IClientRegistry;
import zx.zxlive.core.server.api.IMappingStrategy;
import zx.zxlive.core.server.api.persistence.IPersistenceStore;
import zx.zxlive.core.server.api.scope.IScope;
import zx.zxlive.core.server.api.service.IServiceInvoker;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * <p>
 * This is basic context implementation used by Red5.
 * </p>
 */
//@MXBean
public interface ContextBean {

    public IScope getGlobalScope();

    public IScope resolveScope(String path);

    public IScope resolveScope(IScope root, String path);

    public IPersistenceStore getPersistanceStore();

    public ApplicationContext getApplicationContext();

    public void setContextPath(String contextPath);

    public IClientRegistry getClientRegistry();

    public IScope getScope();

    public IServiceInvoker getServiceInvoker();

    public Object lookupService(String serviceName);

    public IScopeHandler lookupScopeHandler(String contextPath);

    public IMappingStrategy getMappingStrategy();

    public Resource[] getResources(String pattern) throws IOException;

    public Resource getResource(String path);

    public IScope resolveScope(String host, String path);

    public Object getBean(String beanId);

    public Object getCoreService(String beanId);

}
