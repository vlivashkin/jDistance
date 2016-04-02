package com.jdistance.impl.adapter.graph;

import com.jdistance.graph.Graph;
import com.jdistance.graph.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class DCRGraphMLReader {
    public Graph importGraph(File file) throws ParserConfigurationException, IOException, SAXException {
        LinkedList<NodeData> linkedList = new LinkedList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        NodeList nodeList = doc.getElementsByTagName("node");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            NodeData nodeData = new NodeData();
            NodeList childNodes = nodeList.item(i).getChildNodes();
            for (int k = 0; k < childNodes.getLength(); ++k) {
                if (childNodes.item(k).getAttributes() != null) {
                    String prefix = childNodes.item(k).getAttributes().getNamedItem("key").getNodeValue();
                    String value = childNodes.item(k).getFirstChild().getNodeValue();
                    if ("d102".equals(prefix)) {
                        nodeData.setNodeId(Integer.valueOf(value));
                    } else if ("d7".equals(prefix)) {
                        nodeData.setActive(Boolean.valueOf(value));
                    } else if ("d4".equals(prefix)) {
                        nodeData.setColor(value);
                    }
                }
            }
            linkedList.add(nodeData);
        }
        LinkedList<NodeData> activeNodeData = new LinkedList<>();
        LinkedList<EdgeData> activeEdgeData = new LinkedList<>();

        activeNodeData.addAll(linkedList.stream().filter(NodeData::getActive).collect(Collectors.toList()));
        NodeList edgeList = doc.getElementsByTagName("edge");
        for (int i = 0; i < edgeList.getLength(); ++i) {
            EdgeData edgeData = new EdgeData();
            NodeList childEdges = edgeList.item(i).getChildNodes();
            for (int k = 0; k < childEdges.getLength(); ++k) {
                if (childEdges.item(k).getAttributes() != null) {
                    String prefix = childEdges.item(k).getAttributes().getNamedItem("key").getNodeValue();
                    String value = childEdges.item(k).getFirstChild().getNodeValue();
                    if ("d15".equals(prefix)) {
                        edgeData.setActive(Boolean.valueOf(value));
                    }
                }
            }
            if (edgeData.getActive()) {
                edgeData.setSource(edgeList.item(i).getAttributes().getNamedItem("source").getNodeValue());
                edgeData.setTarget(edgeList.item(i).getAttributes().getNamedItem("target").getNodeValue());
                activeEdgeData.add(edgeData);
            }

        }
        Collections.sort(activeNodeData);

        String color = "";
        if (activeNodeData.size() > 0) {
            color = activeNodeData.get(0).getColor();
        }
        for (NodeData aActiveNodeData : activeNodeData) {
            if (!color.equals(aActiveNodeData.getColor())) {
                color = aActiveNodeData.getColor();
            }
        }
        HashMap<Integer, Integer> order = new HashMap<>();
        for (int i = 0; i < activeNodeData.size(); ++i) {
            order.put(activeNodeData.get(i).getNodeId(), i);
        }

        double[][] sparseM = new double[activeNodeData.size()][activeNodeData.size()];
        for (EdgeData aActiveEdgeData : activeEdgeData) {
            String source = aActiveEdgeData.getSource();
            String target = aActiveEdgeData.getTarget();
            Integer intSource = order.get(source);
            Integer intTarget = order.get(target);
            sparseM[intSource][intTarget] = (double) 1;
            sparseM[intTarget][intSource] = (double) 1;
        }

        ArrayList<Node> simpleNode = activeNodeData.stream()
                .map(aActiveNodeData -> new Node(aActiveNodeData.getNodeId(), aActiveNodeData.getColor()))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Graph(simpleNode, sparseM);
    }

    private class EdgeData {
        String source;
        String target;
        Boolean active;

        Boolean getActive() {
            return active;
        }

        void setActive(Boolean active) {
            this.active = active;
        }

        String getSource() {
            return source;
        }

        void setSource(String source) {
            this.source = source;
        }

        String getTarget() {
            return target;
        }

        void setTarget(String target) {
            this.target = target;
        }
    }

    private class NodeData implements Comparable<NodeData> {
        private int nodeId;
        private String color;
        private Boolean active;

        Boolean getActive() {
            return active;
        }

        void setActive(Boolean active) {
            this.active = active;
        }

        int getNodeId() {
            return nodeId;
        }

        void setNodeId(int nodeId) {
            this.nodeId = nodeId;
        }

        String getColor() {
            return color;
        }

        void setColor(String color) {
            this.color = color;
        }

        @Override
        public int compareTo(NodeData o) {
            if (this.getColor() != null) {
                return this.getColor().compareTo(o.getColor());
            } else return 0;
        }
    }
}