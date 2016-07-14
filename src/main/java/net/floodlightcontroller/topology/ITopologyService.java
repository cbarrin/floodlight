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

package net.floodlightcontroller.topology;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.routing.Link;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface ITopologyService extends IFloodlightService  {

	public enum PATH_METRIC { LATENCY, HOPCOUNT, HOPCOUNT_AVOID_TUNNELS, UTILIZATION, LINK_SPEED };

	public PATH_METRIC setPathMetric(PATH_METRIC metric);
	public PATH_METRIC getPathMetric();

	/*******************************************************
	 * GENERAL TOPOLOGY FUNCTIONS
	 *******************************************************/
	
	/**
	 * Add a listener to be notified upon topology events.
	 * @param listener
	 */
	public void addListener(ITopologyListener listener);

	/**
	 * Retrieve the last time the topology was computed.
	 * @return
	 */
	public Date getLastUpdateTime();

	/*******************************************************
	 * PORT FUNCTIONS
	 *******************************************************/
	
	/**
	 * Determines if a device can be learned/located on this switch+port.
	 * @param switchid
	 * @param port
	 * @return
	 */
	public boolean isAttachmentPointPort(DatapathId switchid, OFPort port);

	/**
	 * Determines whether or not a switch+port is a part of
	 * a link or is a leaf of the network.
	 * @param sw
	 * @param p
	 * @return
	 */
   	public boolean isEdge(DatapathId sw, OFPort p);
   	
	/**
	 * Get list of ports that can SEND a broadcast packet.
	 * @param sw
	 * @return
	 */
	public Set<OFPort> getSwitchBroadcastPorts(DatapathId sw);
	
	/**
	 * Checks if the switch+port is in the broadcast tree.
	 * @param sw
	 * @param port
	 * @return
	 */
	public boolean isBroadcastDomainPort(DatapathId sw, OFPort port);

	/**
	 * Determines if the switch+port is blocked. If blocked, it
	 * should not be allowed to send/receive any traffic.
	 * @param sw
	 * @param portId
	 * @return
	 */
	public boolean isAllowed(DatapathId sw, OFPort portId);

	/**
	 * Indicates if an attachment point on the new switch port is consistent
	 * with the attachment point on the old switch port or not.
	 * @param oldSw
	 * @param oldPort
	 * @param newSw
	 * @param newPort
	 * @return
	 */
	public boolean isConsistent(DatapathId oldSw, OFPort oldPort, 
			DatapathId newSw, OFPort newPort);

	/**
	 * Indicates if the two switch ports are connected to the same
	 * broadcast domain or not.
	 * @param s1
	 * @param p1
	 * @param s2
	 * @param p2
	 * @return
	 */
	public boolean isInSameBroadcastDomain(DatapathId s1, OFPort p1,
			DatapathId s2, OFPort p2);

	/** 
	 * Get broadcast ports on a target switch for a given attachment point
	 * point port.
	 * @param targetSw
	 * @param src
	 * @param srcPort
	 * @return
	 */
	public Set<OFPort> getBroadcastPorts(DatapathId targetSw, DatapathId src, OFPort srcPort);

	/**
	 * Checks if the given switch+port is allowed to receive broadcast packets.
	 * @param sw
	 * @param portId
	 * @return
	 */
	public boolean isIncomingBroadcastAllowed(DatapathId sw, OFPort portId);
	
	/**
	 * If the src broadcast domain port is not allowed for incoming
	 * broadcast, this method provides the topologically equivalent
	 * incoming broadcast-allowed src port.
	 * @param src
	 * @param srcPort
	 * @return
	 */
	public NodePortTuple getAllowedIncomingBroadcastPort(DatapathId src, OFPort srcPort);

	/**
	 * Gets the set of ports that belong to a broadcast domain.
	 * @return
	 */
	public Set<NodePortTuple> getBroadcastDomainPorts();
	
	/**
	 * Gets the set of ports that belong to tunnels.
	 * @return
	 */
	public Set<NodePortTuple> getTunnelPorts();

	/**
	 * Returns a set of blocked ports.  The set of blocked
	 * ports is the union of all the blocked ports across all
	 * instances.
	 * @return
	 */
	public Set<NodePortTuple> getBlockedPorts();

	/**
	 * Returns the enabled, non quarantined ports of the given switch. Returns
	 * an empty set if switch doesn't exists, doesn't have any enabled port, or
	 * has only quarantined ports. Will never return null.
	 */
	public Set<OFPort> getPorts(DatapathId sw);
	
	/*******************************************************
	 * ISLAND/DOMAIN/CLUSTER FUNCTIONS
	 *******************************************************/
	
	/**
	 * Return the ID of the domain/island/cluster this switch is
	 * a part of. The ID is the lowest switch DPID within the domain.
	 * @param switchId
	 * @return
	 */
	public DatapathId getOpenflowDomainId(DatapathId switchId);
	
	/**
	 * Determines if two switches are in the same domain/island/cluster.
	 * @param switch1
	 * @param switch2
	 * @return true if the switches are in the same cluster
	 */
	public boolean inSameOpenflowDomain(DatapathId switch1, DatapathId switch2);

	/**
	 * Gets all switches in the same domain/island/cluster as the switch provided.
	 * @param switchDPID
	 * @return
	 */
	public Set<DatapathId> getSwitchesInOpenflowDomain(DatapathId switchDPID);
	
	/*******************************************************
	 * LINK FUNCTIONS
	 *******************************************************/
	
	/**
	 * Get all network links, including intra-cluster and inter-cluster links. 
	 * Links are grouped for each DatapathId separately.
	 * @return
	 */
	public Map<DatapathId, Set<Link>> getAllLinks();
	
	/**
	 * Gets a list of ports on a given switch that are part of known links.
	 * @param sw
	 * @return
	 */
	public Set<OFPort> getPortsWithLinks(DatapathId sw);

	/*******************************************************
	 * PATH-FINDING FUNCTIONS
	 *******************************************************/
	
	/**
	 * If trying to route a packet ingress a source switch+port to a destination
	 * switch+port, retrieve the egress source switch+port leading to the destination.
	 * @param src
	 * @param srcPort
	 * @param dst
	 * @param dstPort
	 * @return
	 */
	public NodePortTuple getOutgoingSwitchPort(DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);

	/**
	 * If trying to route a packet ingress a source switch+port to a destination
	 * switch+port, retrieve the ingress destination switch+port leading to the destination.
	 * @param src
	 * @param srcPort
	 * @param dst
	 * @param dstPort
	 * @return
	 */
	public NodePortTuple getIncomingSwitchPort(DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);

	/**
	 * If the dst is not allowed by the higher-level topology,
	 * this method provides the topologically equivalent broadcast port.
	 * @param src
	 * @param dst
	 * @return the allowed broadcast port
	 */
	public NodePortTuple getAllowedOutgoingBroadcastPort(DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);
}