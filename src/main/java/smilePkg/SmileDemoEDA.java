/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smilePkg;

/**
 *
 * @author SaiF El-deen
 */
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.measure.NominalScale;
import smile.data.vector.IntVector;
//import smile.plot.swing.Histogram;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import smile.plot.swing.Histogram;

public class SmileDemoEDA {
    String trainPath = "src/main/resources/data/titanic_train.csv";
    String testPath = "src/main/resources/data/titanic_test.csv";

    public static void main(String[] args) throws IOException {
        SmileDemoEDA sd = new SmileDemoEDA ();
        PassengerProvider pProvider = new PassengerProvider ();
        DataFrame trainData = pProvider.readCSV (sd.trainPath);
        System.out.println (trainData.structure ());
        System.in.read();
        System.out.println (trainData.summary ());
        System.in.read();
        trainData = trainData.merge (IntVector.of ("Gender",encodeCategory (trainData, "Sex")));
        System.out.println (trainData.structure ());
        System.in.read();
        System.out.println (trainData.summary ());
        System.in.read();
        System.out.println("Here");
        
        trainData = trainData.merge (IntVector.of ("PClassValues",encodeCategory (trainData, "Pclass")));

        System.out.println ("=======Encoding Non Numeric Data==============");
        System.out.println (trainData.structure ());
        System.out.println (trainData);
         System.in.read();
        System.out.println ("=======Dropping the Name, Pclass, and Sex Columns==============");
        trainData = trainData.drop ("Name");
        trainData=trainData.drop("Pclass");
        trainData=trainData.drop("Sex");
        System.out.println (trainData.structure ());
         System.in.read();
        System.out.println (trainData.summary ());
         System.in.read();
        trainData = trainData.omitNullRows ();
        System.out.println ("=======After Omitting null Rows==============");
        System.out.println (trainData.summary ());
         System.in.read();
        System.out.println ("=======Start of Explaratory Data Analysis==============");
        try {
            eda (trainData);
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace ();
        }
        RandomForest model = RandomForest.fit(Formula.lhs("Survived"), trainData);
        System.out.println("feature importance:");
        System.out.println(Arrays.toString(model.importance()));
        System.out.println(model.metrics ());
        //TODO load test data to validate model

    }

    public static int[] encodeCategory(DataFrame df, String columnName) {
        String[] values = df.stringVector (columnName).distinct ().toArray (new String[]{});
        int[] pclassValues = df.stringVector (columnName).factorize (new NominalScale (values)).toIntArray ();
        return pclassValues;
    }

    private static void eda(DataFrame titanic) throws InterruptedException, InvocationTargetException {
        titanic.summary ();
        DataFrame titanicSurvived = DataFrame.of (titanic.stream ().
                                                                    filter (t -> t.get ("Survived").equals (1)));
       
        titanicSurvived = titanicSurvived.omitNullRows ();
        
        int size = titanicSurvived.size ();
        System.out.println (size + " are survived");
        Double averageAge = titanicSurvived.stream ()
                                                .mapToDouble (t -> t.isNullAt ("Age") ? 0.0 : t.getDouble ("Age"))
                                                .average ()
                                                .orElse (0);
        System.out.println (averageAge.intValue ()+" is age average for survived passenger");
        
        Histogram.of (titanicSurvived.doubleVector ("Age").toDoubleArray (), 15, false)
                                                                                    .canvas ().setAxisLabels ("Age", "Count")
                                                                                    .setTitle ("Age frequencies among surviving passengers")
                                                                                    .window ();
        
        Histogram.of (titanicSurvived.intVector ("PClassValues").toIntArray (), 4, true)
                                                                                    .canvas ().setAxisLabels ("Classes", "Count")
                                                                                    .setTitle ("Pclass values frequencies among surviving passengers")
                                                                                    .window ();

        //////////////////////////////////////////////////////////////////////////

    }
}
