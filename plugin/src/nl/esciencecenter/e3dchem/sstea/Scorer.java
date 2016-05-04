package nl.esciencecenter.e3dchem.sstea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.crypto.KeySelectorException;

public class Scorer {
    /**
     * Nr of all amino acids and gap (-).
     */
    private int distinctAA = 21;

    public List<Score> scoreit(Map<String, String> sequences, Set<String> subfamily_members) throws KeySelectorException {
        if (sequences.size() < 2) {
            throw new IllegalArgumentException("Not enough sequences");
        }
        if (subfamily_members.isEmpty()) {
            throw new IllegalArgumentException("Not enough subfamily_members");
        }
        if (!sequences.keySet().containsAll(subfamily_members)) {
            throw new KeySelectorException("Not all subfamily_members are in keys of sequences");
        }

        int minimumSequenceLength = Collections.max(sequences.values()).length();
        int outSideNr = sequences.size() - subfamily_members.size();
        int inSideNr = subfamily_members.size();

        ArrayList<Score> scores = new ArrayList<Score>();
        for (int i = 0; i < minimumSequenceLength; i++) {
            HashMap<String, Integer> outsideCounts = new HashMap<String, Integer>();
            HashMap<String, Integer> insideCounts = new HashMap<String, Integer>();
            for (Entry<String, String> entry : sequences.entrySet()) {
                String sequence = entry.getValue();
                String aminoAcid = sequence.substring(i, i + 1);
                if (subfamily_members.contains(entry.getKey())) {
                    if (insideCounts.containsKey(aminoAcid)) {
                        insideCounts.put(aminoAcid, insideCounts.get(aminoAcid) + 1);
                    } else {
                        insideCounts.put(aminoAcid, 1);
                    }
                } else {
                    if (outsideCounts.containsKey(aminoAcid)) {
                        outsideCounts.put(aminoAcid, outsideCounts.get(aminoAcid) + 1);
                    } else {
                        outsideCounts.put(aminoAcid, 1);
                    }
                }
            }
            int outsideVariability = outsideCounts.size();
            double outsideFi = 0;
            for (Integer count : outsideCounts.values()) {
                outsideFi += (double) count / (double) outSideNr * Math.log((double) count / (double) outSideNr);
            }
            double outsideEntropy = Math.abs(-1.0 * outsideFi);
            int insideVariability = insideCounts.size();
            double insideFi = 0;
            for (Integer count : insideCounts.values()) {
                insideFi += (double) count / (double) inSideNr * Math.log((double) count / (double) inSideNr);
            }
            double insideEntropy = Math.abs(-1.0 * insideFi);
            double score = Math
                    .sqrt(Math.pow(Math.abs(Math.log(1.0 / distinctAA)) - outsideEntropy, 2) + Math.pow(insideEntropy, 2));
            scores.add(new Score(i + 1, score, insideEntropy, outsideEntropy, insideVariability, outsideVariability));
        }

        return scores;
    }

    public int getDistinctAA() {
        return distinctAA;
    }

    public void setDistinctAA(int distinctAA) {
        this.distinctAA = distinctAA;
    }

}
