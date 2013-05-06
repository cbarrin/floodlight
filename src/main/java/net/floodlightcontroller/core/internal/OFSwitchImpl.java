/**
*    Copyright 2012, Big Switch Networks, Inc.
*    Originally created by David Erickson, Stanford University
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

package net.floodlightcontroller.core.internal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openflow.protocol.statistics.OFDescriptionStatistics;

import net.floodlightcontroller.core.OFSwitchBase;

/**
 * This is the internal representation of an openflow switch.
 */
public class OFSwitchImpl extends OFSwitchBase {

    @Override
    @JsonIgnore
    public void setSwitchProperties(OFDescriptionStatistics description) {
        this.description = new OFDescriptionStatistics(description);
    }

    @Override
    public OFPortType getPortType(short port_num) {
        return OFPortType.NORMAL;
    }

    @Override
    @JsonIgnore
    public boolean isFastPort(short port_num) {
        return false;
    }

    @Override
    public List<Short> getUplinkPorts() {
        return null;
    }
}
