/**
 *    Copyright 2011,2012 Big Switch Networks, Inc. 
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

package net.floodlightcontroller.routing.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.topology.NodePortTuple;

import java.io.IOException;

public class RouteSerializer extends JsonSerializer<Route> {

	@Override
	public void serialize(Route route, JsonGenerator jGen, SerializerProvider serializer)
			throws IOException, JsonProcessingException {
		jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

		jGen.writeStartObject();
		//jGen.writeNumberField("cookie", route.getId().getCookie().getValue());
		jGen.writeStringField("src_dpid", route.getId().getSrc().toString());
		jGen.writeStringField("dst_dpid", route.getId().getDst().toString());
		jGen.writeStringField("hop_count", new Integer(route.getRouteHopCount()).toString());
		//jGen.writeStringField("latency", route.getRouteLatency().toString());
		//jGen.writeNumberField("route_count", route.getRouteCount());
		jGen.writeFieldName("path");
		jGen.writeStartArray();
		for (NodePortTuple npt : route.getPath()) {
			jGen.writeStartObject();
			jGen.writeStringField("dpid", npt.getNodeId().toString());
			jGen.writeNumberField("port", npt.getPortId().getPortNumber());
			jGen.writeEndObject();
		}
		jGen.writeEndArray();
		jGen.writeEndObject();
	}
}