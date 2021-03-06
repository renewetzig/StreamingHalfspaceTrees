package com.rene_wetzig;

import com.rene_wetzig.externalClasses.Sample;
import java.util.concurrent.ThreadLocalRandom;

public class TreeOrchestrator {

    /*
    Based on Streaming Half-Space Trees algorithm from "Fast Anomaly Detection for Streaming Data"
    by Swee Chuan Tan, Kai Ming Ting and Tony Fei Liu, 2011, in Proceedings of the Twenty-Second International
    Joint Conference on Artificial Intelligence

    Implemented and expanded by René Wetzig

    TreeOrchestrator creates and manages Half-Space Trees, which are used to learn a data stream's profile and score
    data points.
     */




    /*
     * Variables
     */
    private static Node[] roots; // saves the roots of the half-space trees

    private int nrOfTrees;
    private int nrOfDimensions; // number of dimensions in the data stream.
    private int maxDepth;  // NOTE a tree with a maxDepth of n has n+1 levels. root is depth ZERO.
    double[] min; // array of minimum values a data point can reach for each dimension
    double[] max; // array of maximum values a data point can reach for each dimension
    private int windowSize;
    private int windowCounter; // keeps track of the number of samples that have been inserted.
    private int sizeLimit; // the minimum number of instances in the reference counter of a node, at which the anomalyScore is calculated.




    public TreeOrchestrator(int nrOfTrees, int maxDepth, int windowSize, int nrOfDimensions, double[] minArray, double[] maxArray, int sizeLimit) {
        this.nrOfTrees = nrOfTrees;
        this.maxDepth = maxDepth;
        this.windowSize = windowSize;
        this.nrOfDimensions = nrOfDimensions;
        this.min = minArray.clone();
        this.max = maxArray.clone();
        this.sizeLimit = sizeLimit;

        this.windowCounter = 0;


        this.roots = new Node[nrOfTrees];

        createTrees();

    }

    private void createTrees(){

        for(int i = 0; i < nrOfTrees; i++) {
            double[] newMin = min.clone();
            double[] newMax = max.clone();

            // Random Perturbation of the datastream's domain in order to generate a more diverse family of Halfspace Trees
            for(int j = 0; j < nrOfDimensions; j++){

                double distance = newMax[j] - newMin[j];
                double randomPoint = ThreadLocalRandom.current().nextDouble();

                if(randomPoint < 0.5) {
                    newMax[j] = newMin[j] + (randomPoint*distance) + 2.0*((1.0-randomPoint) * distance);
                    newMin[j] = newMin[j] + (randomPoint*distance) - 2.0*((1.0-randomPoint) * distance);
                } else {
                    newMax[j] = newMin[j] + (randomPoint*distance) + 2.0*(randomPoint * distance);
                    newMin[j] = newMin[j] + (randomPoint*distance) - 2.0*(randomPoint * distance);
                }

                // System.out.println("NewMin[" + j + "] = "+ newMin[j]);
                // System.out.println("NewMax[" + j + "] = "+ newMax[j]);

            }

            roots[i] = new Node(0, maxDepth, nrOfDimensions, newMin, newMax, sizeLimit, null);
        }

    }

    // We use Sample class by CIT, TU Berlin, for our data points in order to allow for easier integration of the algorithm
    // in their testing environment
    public int insertSample(Sample newSample){
        // if the latest window is full, replace the reference mass with the latest mass in all trees.
        windowCounter++;
        if(windowCounter >= windowSize){
            for(int i=0;i<nrOfTrees;i++){
                roots[i].updateReference();
            }
            windowCounter = 0;
        }

        int anomalyScore;
        anomalyScore = 0;
        for (int i = 0; i< nrOfTrees; i++){
            anomalyScore = anomalyScore + roots[i].insertSample(newSample, false, 0);
        }
        return anomalyScore;
    }

    public String toString(){
        String trees = "";

        for (int i=0; i < nrOfTrees; i++){
            trees = trees + "\n" + "\n" + "\n" + "Tree Nr. " + i + "\n" + singleTreeToString(i);
        }

        return trees;
    }

    public String singleTreeToString(int tree){

        String output = "";
        int maxWidth = (int) Math.pow(2, maxDepth);
        String[][] treeString = new String[maxDepth+1][maxWidth+1];
        Node activeNode;

        activeNode = roots[tree];

        int nrOfNodesInTree = 0; // calculates the total number of nodes in the tree
        for (int i = 0; i <= maxDepth; i++){
            nrOfNodesInTree = nrOfNodesInTree + (int) Math.pow(2,i);
        }

        int depth = 0;
        int width = 0;
        treeString[depth][width] = activeNode.toString(); //adds the root to the string

        int nodesDone = 1;


        // Traverses the tree and saves nodes' values.
        while(width < maxWidth-1){
            if(activeNode.amLeaf){
                if(activeNode == activeNode.parent.leftChild) {
                    activeNode = activeNode.parent.rightChild;
                    width++;
                } else {
                    while(!activeNode.amRoot && activeNode == activeNode.parent.rightChild) {
                        activeNode = activeNode.parent;
                        depth--;
                        width = width / 2;
                    }
                    activeNode = activeNode.parent.rightChild;
                    width++;
                }

            } else {
                activeNode = activeNode.leftChild;
                depth++;
                width = 2*width;
            }

            treeString[depth][width] = activeNode.toString();

            // Code to check how many nodes have been done.
            //  nodesDone++;
            //  System.out.println("d="+ depth + ", w=" + width + ", nr. " + nodesDone);

        }


        // This is where the output is constructed to resemble a tree.
        for ( int i = 0; i <= maxDepth; i++){
            int seperatorsThisDepth;
            int nodesThisDepth;

            nodesThisDepth = (int) Math.pow(2,i);
            seperatorsThisDepth = (int) Math.ceil( maxWidth / (nodesThisDepth+1));

            for (int j = 0 ; j < nodesThisDepth; j++){
                for(int seps = 1; seps <= seperatorsThisDepth; seps++){
                    output = output +"\t"+ "\t" + "\t" + "\t";
                }
                output = output + " " + treeString[i][j];
            }
            output = output + "\n";
        }



    return output;

    }


}
