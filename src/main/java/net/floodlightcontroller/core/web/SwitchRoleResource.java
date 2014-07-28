/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.core.web;

import java.util.HashMap;

import org.projectfloodlight.openflow.types.DatapathId;
import org.restlet.resource.ServerResource;

import net.floodlightcontroller.core.HARole;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.RoleInfo;
import net.floodlightcontroller.core.internal.IOFSwitchService;

import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchRoleResource extends ServerResource {

    protected static Logger log = LoggerFactory.getLogger(SwitchRoleResource.class);

    @Get("json")
    public Object getRole() {
    	IOFSwitchService switchService =
                (IOFSwitchService)getContext().getAttributes().
                    get(IOFSwitchService.class.getCanonicalName());

        String switchId = (String) getRequestAttributes().get("switchId");

        RoleInfo roleInfo;

        if (switchId.equalsIgnoreCase("all")) {
            HashMap<String,RoleInfo> model = new HashMap<String,RoleInfo>();
            for (IOFSwitch sw: switchService.getAllSwitchMap().values()) {
                switchId = sw.getStringId();
                //TODO @Ryan not sure what the changeDescription string should be here.
                roleInfo = new RoleInfo(HARole.ofOFRole(sw.getControllerRole()), "", null);
                model.put(switchId, roleInfo);
            }
            return model;
        }

        DatapathId dpid = DatapathId.of(switchId);
        IOFSwitch sw = switchService.getSwitch(dpid);
        if (sw == null)
            return null;
        //TODO @Ryan not sure what the changeDescription string should be here.
        roleInfo = new RoleInfo(HARole.ofOFRole(sw.getControllerRole()), "", null);
        return roleInfo;
    }
}
