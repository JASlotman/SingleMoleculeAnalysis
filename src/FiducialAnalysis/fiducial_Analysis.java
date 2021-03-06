package FiducialAnalysis;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;


public class fiducial_Analysis implements PlugIn {

    public void run(String arg){
        ResultsTable rt = Analyzer.getResultsTable();
        int gap =50;
        double minOn = 50;
        int factor = 2;

        //establish bead parameters

        GenericDialog gd = new GenericDialog("Fiducial_Parameters");
        gd.addNumericField("Maximum_gap size",(double)gap,0);
        gd.addNumericField("Track_length (percentage of max)",minOn,0);
        gd.addNumericField("Search Factor",factor,0);
        gd.showDialog();

        if(gd.wasCanceled()){
            return;
        }

        gap = (int) gd.getNextNumber();
        minOn = (int) gd.getNextNumber();
        factor = (int) gd.getNextNumber();




        if(rt==null){
            IJ.showMessage("No Results table is open");
            return;
        }

        localisationList locList = new localisationList(rt);
        IJ.log("localisation loaded");

        locList.assignTracks(gap,factor);
        IJ.log("tracks assigned");

        int frames = locList.getMaxF();

        IJ.log("aantal frames "+frames);

        locList.getTracks().FilterIds((int)(frames*(minOn/100)));

        trackList track = locList.getTracks();
        int[] ids = track.getFilteredIDs();
        int[] lengths = track.getFilteredlengths();
        double[] meanX = track.getFilteredmeanXs();
        double[] meanY = track.getFilteredmeanYs();


        for(int i=0;i<ids.length;i++){
            IJ.log(""+ids[i]+" "+lengths[i]+" "+(int) meanX[i]+" "+(int) meanY[i]);
        }

        ImagePlus imp = WindowManager.getCurrentImage();

        if(imp==null){
            double xSize = locList.getMaxX();
            double ySize = locList.getMaxY();

            Calibration c = new Calibration();
            c.pixelWidth = 5.0;
            c.pixelHeight = 5.0;
            c.setUnit("nm");

            ImageProcessor ip = new ByteProcessor((int) c.getRawX(xSize), (int)c.getRawY(ySize));
            imp = new ImagePlus("temp",ip);
            imp.setCalibration(c);

        }


        imp = track.addBeads(imp);
        /*
        int[] trackIDs = track.getTrackLocs(track.getFilteredIDs()[0]);
        localisation[] trackNumbers = locList.getLocList();

        for(int i=0;i<trackIDs.length;i++){
            IJ.log(trackIDs[i]+" "+trackNumbers[trackIDs[i]].getTrackID() +" "+ trackNumbers[trackIDs[i]].getX() );

        }
        */


        imp.show();


        double[] xs = track.getFilteredmeanXs();
        double[] ys = track.getFilteredmeanYs();

        rt = new ResultsTable();

        for(int i=0;i<xs.length;i++){


            rt.incrementCounter();
            rt.addValue("X",xs[i]);
            rt.addValue("Y",ys[i]);


        }


        //rt = locList.makeResultTable();
        rt.show("Results");

        IJ.log("finished");



    }
}
