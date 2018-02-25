/**
 * Created by geddingsbarrineau on 4/1/16.
 */
package net.floodlightcontroller.topology;

import net.floodlightcontroller.routing.BroadcastTree;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.HashSet;
import java.util.Set;

/**
 * An archipelago is defined as a group of OpenFlow islands (or clusters).
 */
public class Archipelago {
    private DatapathId id; // the lowest id of the nodes
    private final Set<Cluster> clusters;
    private BroadcastTree destinationRootedFullTree;

    public Archipelago() {
        id = DatapathId.NONE;
        clusters = new HashSet<>();
        destinationRootedFullTree = null;
    }

    public DatapathId getId() {
        return id;
    }

    protected Set<Cluster> getClusters() {
        return clusters;
    }

    /**
     * Adds a cluster to an archipelago. If the cluster is the first to be added
     * or if the cluster's id is less than the archipelago's current id, the
     * archipelago's id will be replaced with the cluster's id.
     * @param c the cluster to be added
     * @return the archipelago with the cluster added
     */
    Archipelago add(Cluster c) {
        if (clusters.add(c)) {
            if (id.equals(DatapathId.NONE) || c.getId().compareTo(id) < 0) {
                id = c.getId();
            }
        }
        return this;
    }

    boolean isMember(Cluster c) {
        return clusters.contains(c);
    }

    boolean isMember(DatapathId id) {
        for (Cluster c : clusters) {
            if(c.getNodes().contains(id)) return true;
        }
        return false;
    }

    /**
     * Merges an archipelago into the existing archipelago.
     * The existing archipelago's id will be replaced if its id is greater than the archipelago being merged.
     * @param a the archipelago to merge with
     */
    void merge(Archipelago a) {
        clusters.addAll(a.getClusters());
        if (id.equals(DatapathId.NONE) || !a.getId().equals(DatapathId.NONE) || a.getId().compareTo(id) < 0) {
            id = a.getId();
        }
    }
    
    Set<DatapathId> getSwitches() {
        Set<DatapathId> allSwitches = new HashSet<>();
        for (Cluster c : clusters) {
            allSwitches.addAll(c.getNodes());
        }
        return allSwitches;
    }

    BroadcastTree getBroadcastTree() {
        return destinationRootedFullTree;
    }

    void setBroadcastTree(BroadcastTree bt) {
        destinationRootedFullTree = bt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Archipelago that = (Archipelago) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return clusters != null ? clusters.equals(that.clusters) : that.clusters == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (clusters != null ? clusters.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "[Archipelago id=" + id.toString() + ", " + clusters.toString() + "]";
    }
}
