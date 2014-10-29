package ru.isa.ai.htm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Author: Aleksandr Panov
 * Date: 29.10.2014
 * Time: 12:47
 */
public class AgglomerativeHierarchicalClusterer {

    private List<MarkovNode> markovNet = new ArrayList<>();
    private int[] clusterNumbers;
    private int maxCLusterNumber;

    public AgglomerativeHierarchicalClusterer(List<MarkovNode> markovNet, int maxCLusterNumber) {
        this.markovNet = markovNet;
        this.maxCLusterNumber = maxCLusterNumber;
    }

    public void buildClusters(){
        int nInstances = markovNet.size();
        clusterNumbers = new int[nInstances];

        if (nInstances > 1) {
            List<Integer>[] nClusterID = new ArrayList[nInstances];

            for (int i = 0; i < nInstances; i++) {
                nClusterID[i] = new ArrayList<>();
                nClusterID[i].add(i);
            }
            // calculate distance matrix
            // used for keeping track of hierarchy
            ClusterNode[] clusterNodes = new ClusterNode[nInstances];
            doLinkClustering(nInstances, nClusterID, clusterNodes);

            // move all clusters in m_nClusterID array
            // & collect hierarchy
            int iCurrent = 0;

            for (int i = 0; i < nInstances; i++) {
                if (nClusterID[i].size() > 0) {
                    for (int j = 0; j < nClusterID[i].size(); j++) {
                        clusterNumbers[nClusterID[i].get(j)] = iCurrent;
                    }
                    iCurrent++;
                }
            }
        } else if (nInstances == 1) {
            clusterNumbers[0] = 0;
        }
    }


    private void doLinkClustering(int nInstances, List<Integer>[] nClusterID, ClusterNode[] clusterNodes) {
        int nClusters = nInstances;
        PriorityQueue<Tuple> queue = new PriorityQueue<>(nClusters * nClusters / 2, new TupleComparator());
        double[][] fDistance0 = new double[nClusters][nClusters];

        for (int i = 0; i < nClusters; i++) {
            fDistance0[i][i] = 0;
            for (int j = i + 1; j < nClusters; j++) {
                fDistance0[i][j] = getDistance(nClusterID[i].get(0), nClusterID[j].get(0));
                fDistance0[j][i] = fDistance0[i][j];
                queue.add(new Tuple(fDistance0[i][j], i, j, 1, 1));
            }
        }
        while (nClusters > maxCLusterNumber) {
            // find closest two clusters
            // use priority queue to find next best pair to cluster
            Tuple t;
            do {
                t = queue.poll();
            } while (t != null
                    && (nClusterID[t.m_iCluster1].size() != t.m_nClusterSize1 || nClusterID[t.m_iCluster2]
                    .size() != t.m_nClusterSize2));
            int iMin1 = t.m_iCluster1;
            int iMin2 = t.m_iCluster2;
            merge(iMin1, iMin2, t.m_fDist, t.m_fDist, nClusterID, clusterNodes);
            // merge clusters

            // update distances & queue
            for (int i = 0; i < nInstances; i++) {
                if (i != iMin1 && nClusterID[i].size() != 0) {
                    int i1 = Math.min(iMin1, i);
                    int i2 = Math.max(iMin1, i);
                    double fDistance = getDistance(fDistance0, nClusterID[i1], nClusterID[i2]);
                    queue.add(new Tuple(fDistance, i1, i2, nClusterID[i1].size(),
                            nClusterID[i2].size()));
                }
            }

            nClusters--;
        }
    }

    private void merge(int iMin1, int iMin2, double fDist1, double fDist2,
                       List<Integer>[] nClusterID, ClusterNode[] clusterNodes) {
        if (iMin1 > iMin2) {
            int h = iMin1;
            iMin1 = iMin2;
            iMin2 = h;
            fDist1 = fDist2;
        }
        nClusterID[iMin1].addAll(nClusterID[iMin2]);
        nClusterID[iMin2].clear();

        // track hierarchy
        ClusterNode node = new ClusterNode();
        if (clusterNodes[iMin1] == null) {
            node.m_iLeftInstance = iMin1;
        } else {
            node.m_left = clusterNodes[iMin1];
            clusterNodes[iMin1].m_parent = node;
        }
        if (clusterNodes[iMin2] == null) {
            node.m_iRightInstance = iMin2;
        } else {
            node.m_right = clusterNodes[iMin2];
            clusterNodes[iMin2].m_parent = node;
        }

        node.m_fHeight = fDist1;

        clusterNodes[iMin1] = node;
    }

    private double getDistance(int index1, int index2) {
        double result = Double.MAX_VALUE;
        MarkovNode node1 = markovNet.get(index1);
        MarkovNode node2 = markovNet.get(index2);
        if (node1.getConnectedNode().containsKey(node2)) {
            result = 1 / node1.getConnectedNode().get(node2);
        }
        if (node2.getConnectedNode().containsKey(node1)) {
            double dist = 1 / node2.getConnectedNode().get(node1);
            if (dist < result)
                result = dist;
        }
        return result;
    }

    private double getDistance(double[][] fDistance, List<Integer> cluster1, List<Integer> cluster2) {
        // find single link distance aka minimum link, which is the closest
        // distance between
        // any item in cluster1 and any item in cluster2
        double fBestDist = Double.MAX_VALUE;
        for (Integer aCluster1 : cluster1) {
            for (Integer aCluster2 : cluster2) {
                double fDist = fDistance[aCluster1][aCluster2];
                if (fBestDist > fDist) {
                    fBestDist = fDist;
                }
            }
        }
        return fBestDist;
    }

    public int[] getClusterNumbers() {
        return clusterNumbers;
    }

    /**
     * used for priority queue for efficient retrieval of pair of clusters to
     * merge
     */
    private class ClusterNode implements Serializable {
        ClusterNode m_left;
        ClusterNode m_right;
        ClusterNode m_parent;
        int m_iLeftInstance;
        int m_iRightInstance;
        double m_fHeight = 0;
    }

    class Tuple {
        double m_fDist;
        int m_iCluster1;
        int m_iCluster2;
        int m_nClusterSize1;
        int m_nClusterSize2;

        public Tuple(double d, int i, int j, int nSize1, int nSize2) {
            m_fDist = d;
            m_iCluster1 = i;
            m_iCluster2 = j;
            m_nClusterSize1 = nSize1;
            m_nClusterSize2 = nSize2;
        }
    }

    /**
     * comparator used by priority queue *
     */
    class TupleComparator implements Comparator<Tuple> {
        @Override
        public int compare(Tuple o1, Tuple o2) {
            if (o1.m_fDist < o2.m_fDist) {
                return -1;
            } else if (o1.m_fDist == o2.m_fDist) {
                return 0;
            }
            return 1;
        }
    }

}
