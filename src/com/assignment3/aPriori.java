package com.assignment3;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by trent on 5/31/2017.
 * This is for assignment 3
 * A Priori will work in the following way:
 * Input: Data set and support value (default given if nothing)
 * The output will be a subset of items which have support more than S
 *
 * Variables:
 * map "frequency_firstPass"
 * map(map "baskets_original"
 * map "candidates_firstPass"
 * map(arrayList, int) "secondPass"
 * map(arrayList, int) "candidates_secondPass"
 * map(arrayList, int) "thirdPass"
 * map(arrayList, int) "candidates_thirdPass"
 * map(arrayList, int) "fourthPass"
 * map(arrayList, int) "candidates_fourthPass"
 * integer "num_baskets"
 * integer "num_items"
 *
 *
 * Method:
 * 1. Read in the file, line by line
 * For each line, num_baskets++ : for each item num_items++
 * For each item, add value 1 to frequency_firstpass map entry at item
 * In the Basket entry of the baskets_original map, add true to the individual number entry of the enclosed map
 *
 * 2. K=1, we are looking for the frequent individual items
 * For each item in the frequency_firstPass Map, if the value is larger than our threshold, add to candidates_firstpass
 *
 * 3. K=2, we are looking for frequent tuples
 * For x in candidates_firstPass,
 *      For y in candidates_firstPass
 *          if x!=y, for baskets_original, if x && y == true,
 *          create arrayList, add x and y (TODO USE A FUNCTION TO INSERT SMALLEST # FIRST)
 *          secondPass.add(tempArrayList, ++) < increase value of entry tempArrayList
 *      For items in secondPass map:
 *          If getValue > threshold, add x and y to candidates_secondPass
 *
 * 4. K=3, we are looking for frequent triples
 *
 * For x in candidates_secondPass,
 *      For y in candidates_secondPass
 *          For z in candidates_secondPass
 *              if x!=y!=z, for (baskets_original), if x && y && z == true,
 *              create arrayList, add x and y and z (TODO USE A FUNCTION TO INSERT SMALLEST # FIRST)
 *              thirdPass.add(tempArrayList, ++) < increase value of entry tempArrayList
 *      For items in thirdPass map:
 *          If getValue > threshold, add x and y and z to candidates_thirdPass
 *
 * 5. K = 4, we are looking for frequent lots of 4.
 *  For X (for Y ( For Z, (For J in candidates_thirdPass
 *      If x!=y!=z!=j, for baskets_original, if (xyzj)=true
 *      create arraylist, add smallest to largest
 *      fourthPass.add(tempArrayList, ++)
 *  For items in the thirdPass map:
 *      if getvalue > threshold, add x and y and z and j to the candidates_fourthPass
 *
 */
public class aPriori {


    /*
    *This function will read in the file
    */
    public static void inputFile(String inputFile, int supportVal) throws IOException {
        /*
        * These counters will take care of trivial tasks
        */
        int unique_item_count = 0;
        int basket_count = 0;
        int total_purchases = 0;
        int aboveThreshold_firstPass = 0;

         /*
         * All data storage within this function is done with the following.
         * Each will be deleted when they are not needed, to save space
         * Maps are used for efficient lookup time.
         */
        TreeMap<Integer, Integer> frequency_firstPass = new TreeMap<Integer, Integer>();
        TreeMap<Integer, TreeMap<Integer, Boolean>> baskets_original = new TreeMap<Integer, TreeMap<Integer, Boolean>>();
        TreeMap<Integer, Integer> candidates_firstPass = new TreeMap<Integer, Integer>();

        TreeMap<ArrayList<Integer>, Integer> thirdpass = new TreeMap<ArrayList<Integer>, Integer>();
        TreeMap<ArrayList<Integer>, Integer> candidates_thirdPass = new TreeMap<ArrayList<Integer>, Integer>();
        TreeMap<ArrayList<Integer>, Integer> fourthPass = new TreeMap<ArrayList<Integer>, Integer>();
        TreeMap<ArrayList<Integer>, Integer> candidates_fourthPass = new TreeMap<ArrayList<Integer>, Integer>();

        /*
        *Status check
        */
        System.out.println("File is being read");

        /*
        * Buffered reader object to read each line into a string
        */
        FileReader in = new FileReader(inputFile);
        BufferedReader br = new BufferedReader(in);
        String line;

        /*
        This will traverse through the entire text file, splitting by whitespace
        Each line will represent a new basket. Each item within it represents a product.
        We will add to our counters, to keep track of which basket/product we are inspecting.
        BasketCount default starts at 0, so will be incremented at end of line.
        Each item will be added to a frequency map, and as a boolean value in the baskets_original map(map)
        using TreeMap<Integer, TreeMap<Integer, Boolean>> baskets_original
        */
        while ((line = br.readLine()) != null) {
            String[] tokenArray = line.split("\\s+");
            for (String token : tokenArray) {
                total_purchases++;
                int currentToken = Integer.parseInt(token);
                frequency_firstPass.merge(currentToken, 1, (a, b) -> a + b);
                if (baskets_original.containsKey(basket_count)) {
                    baskets_original.get(basket_count).put(currentToken, true);
                } else {
                    TreeMap<Integer, Boolean> temp =  new TreeMap<Integer, Boolean>();
                    temp.put(currentToken,true);
                    baskets_original.put(basket_count, temp);
                }
            }
            basket_count++;
        }

        /*
        *Status check
        */
        System.out.println("First Pass: Checking through data to find elements above threshold");

        /*
        * Going through the firstPass data, turning all above the threshold into candidates
        * Candidates will be used in the second pass.
        * For each item in this list, we increment the unique item counter
        */
        for (Map.Entry<Integer, Integer> entry : frequency_firstPass.entrySet()) {
            unique_item_count++;
            if (entry.getValue() > supportVal) {
                aboveThreshold_firstPass++;
                //System.out.println(entry.getValue() + " Is above the support value threshold of " + supportVal);
                candidates_firstPass.put(entry.getKey(), entry.getValue());
            }
        }

        System.out.println(aboveThreshold_firstPass + "/" + unique_item_count + " unique items found above the support value threshold of " + supportVal);
        System.out.println("Individual candidate search finished, looking for itemsets of 2");
        secondK(candidates_firstPass, baskets_original, supportVal);
    }

    public static void secondK (TreeMap<Integer, Integer> candidates_firstPass, TreeMap<Integer, TreeMap<Integer, Boolean>> baskets_original, Integer threshold) {
        /*
        * Input of candidates & basket & threshold allow us to do the following:
        * Check for all permutations of candidates within the basket
        *   If existant, the pair frequency will be incremented
        * All pairs will be evaluated for threshold. Those that pass will be added to candidates_secondPass
        */
        TreeMap<Pair<Integer,Integer>, Integer> secondPass = new TreeMap<Pair<Integer,Integer>, Integer>();
        TreeMap<Integer, Integer> candidates_secondPass = new TreeMap<Integer, Integer>();

        for (Map.Entry<Integer, Integer>  spX : candidates_firstPass.entrySet()) {
            for (Map.Entry<Integer, Integer>  spY : candidates_firstPass.entrySet()) {
                if (!Objects.equals(spX.getKey(), spY.getKey())) {
                    //System.out.println(spX.getKey() + ", " + spY.getKey() + " Being looked at");
                    for (Map.Entry<Integer, TreeMap<Integer, Boolean>> individual_basket : baskets_original.entrySet()){
                        //Could just add to a counter that is created after the if statement above
                        Boolean spX_bool = individual_basket.getValue().containsKey(spX.getKey());
                        Boolean spY_bool = individual_basket.getValue().containsKey(spY.getKey());
                        if (spX_bool &&(spY_bool)){
                            //System.out.println("++Basket contains both of these items");
                            Integer temp_ordered[] = {spX.getKey(), spY.getKey()};
                            Arrays.sort(temp_ordered);
                            Pair<Integer,Integer> curr_xy_secondPass = new Pair<>(temp_ordered[0], temp_ordered[1]);
                            //Go through all of secondPass I guess, then check key by key
                            //todo fix below
                            for (Map.Entry<Pair<Integer,Integer>, Integer> secondPassItem : secondPass.entrySet()) {
                                //getKey brings us the Pair(int,int)
                                System.out.println("SecondPassItem being looked at: " + secondPassItem.getKey().getKey() + "," + secondPassItem.getKey().getValue());
                                if (secondPassItem.getKey().getKey() == curr_xy_secondPass.getKey()
                                        && secondPassItem.getKey().getValue() == curr_xy_secondPass.getValue()) {
                                    //Then the secondPassItem thing contains it
                                    Integer temp = secondPass.get(curr_xy_secondPass);
                                    temp++;
                                    System.out.println("++Basket contains(a,b) w/freq: " + temp);
                                    secondPass.put(curr_xy_secondPass,temp);
                                } else {
                                    secondPass.put(curr_xy_secondPass,1);
                                }
                            }


                            //secondPass.merge(curr_xy_secondPass, 1, (a, b) -> a + b);

                        }
                    }
                }
            }
        }

        System.out.println("Now going to inspect tuples to find candidates with higher frequency than threshold");
        System.out.println("The size of our secondPass is " + secondPass.size());
        /*
        * Now we will inspect all tuples to find the candidates that have a frequency higher than the threshold
        * For this, loop through the secondPass map, check for frequency x, add to candidates_secondpass if higher than x
         */
        for (Map.Entry<Pair<Integer,Integer>, Integer> secondPass_freqCheck : secondPass.entrySet()) {
            if (secondPass_freqCheck.getValue() > threshold) {
               // for (Pair<Integer, Integer> candidate_temp : secondPass_freqCheck.getKey()) {
                Pair<Integer,Integer> candidate_temp = secondPass_freqCheck.getKey();
                    candidates_secondPass.put(candidate_temp.getKey(), 1);
                    candidates_secondPass.put(candidate_temp.getValue(), 1);
                    System.out.println("Val: " + secondPass_freqCheck.getValue() + " for tuple: " + candidate_temp.getKey() + ", " + candidate_temp.getValue());
                //}
            }
        }
    }

    public static void main(String[] args) throws IOException {
    String filenameGiven ="src/T10I4D100K.dat"; //"src/testData.dat";
    int supportValue = 1500; // change this if necessary
    /*
    * Input argument style: 1. filename (string), 2. support value (integer)
    * If we have any inputs, the values will be set to those.
    * Default values are for the testData.dat file and for the support value to be 2.
    */
    if (args.length > 0) {

        /*
        *Status check
        */
        System.out.println("Command line arguments detected, changing values from default.");

        if (args[1] != null) {
            filenameGiven = args[1];
        } if (args[2] != null) {
            supportValue =  Integer.parseInt(args[2]);
        }
    }
    inputFile(filenameGiven, supportValue);
    }


//    @Override
//    public int compareTo(Pair<Integer, Integer> o, Pair<Integer,Integer> o2) {
//        if (o.getValue() == o2.getValue()){
//            if (o.getKey() == o2.getKey()) {
//                return 1;
//            }
//        }
//        return 0;
//    }
}
