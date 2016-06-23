package nl.esciencecenter.e3dchem.sstea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Scorer {
	/**
	 * Nr of all amino acids and maybe gap (-).
	 */
	private int distinctAA;
	private boolean includeGaps;

	public Scorer() {
		includeGaps = false;
		distinctAA = 20;
	}

	public Scorer(boolean includeGaps) {
		this.includeGaps = includeGaps;
		if (includeGaps) {
			distinctAA = 21;
		} else {
			distinctAA = 20;
		}
	}

	public List<Score> scoreit(Map<String, String> sequences, Set<String> subfamily_members) {
		if (sequences.size() < 2) {
			throw new IllegalArgumentException("Not enough sequences");
		}
		if (subfamily_members.isEmpty()) {
			throw new IllegalArgumentException("Not enough subfamily members");
		}
		if (!sequences.keySet().containsAll(subfamily_members)) {
			throw new IllegalArgumentException("Not all subfamily members are identifiers of given sequences");
		}

		int minimumSequenceLength = Collections.min(sequences.values()).length();
		if (minimumSequenceLength == 0) {
			throw new IllegalArgumentException("Empty sequence");
		}

		ArrayList<Score> scores = new ArrayList<Score>();
		for (int i = 0; i < minimumSequenceLength; i++) {
			HashMap<String, Integer> outsideCounts = new HashMap<String, Integer>();
			HashMap<String, Integer> insideCounts = new HashMap<String, Integer>();
			int outSideNr = 0;
			int inSideNr = 0;
			for (Entry<String, String> entry : sequences.entrySet()) {
				String sequence = entry.getValue();
				String aminoAcid = sequence.substring(i, i + 1);
				if (!includeGaps && aminoAcid == "-") {
					continue;
				}
				if (subfamily_members.contains(entry.getKey())) {
					if (insideCounts.containsKey(aminoAcid)) {
						insideCounts.put(aminoAcid, insideCounts.get(aminoAcid) + 1);
					} else {
						insideCounts.put(aminoAcid, 1);
					}
					inSideNr++;
				} else {
					if (outsideCounts.containsKey(aminoAcid)) {
						outsideCounts.put(aminoAcid, outsideCounts.get(aminoAcid) + 1);
					} else {
						outsideCounts.put(aminoAcid, 1);
					}
					outSideNr++;
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
			double score = Math.sqrt(
					Math.pow(Math.abs(Math.log(1.0 / distinctAA)) - outsideEntropy, 2) + Math.pow(insideEntropy, 2));
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
